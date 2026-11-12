import { setupCraftingInput } from './crafting-input.js?v=20261112-2';
import { setHeldItemCursor } from './item-cursor.js';
import { setupInventory } from './inventory.js?v=20261112-5';
import { setupCraftingEffects } from './crafting-effects.js?v=20261112-4';

const element = id => document.getElementById(id);
const grid = Array(9).fill(null);
let challenge = null;
let selected = null;
let solved = false;
let working = false;
let showingSolution = false;
let solutionRequested = false;
const gridElement = element('craft-grid');
const { finishPainting } = setupCraftingInput({
    gridElement, canEdit: canEditGrid, getSelected: () => selected, getCell: index => grid[index], onCellChange: changeCell
});
const effects = setupCraftingEffects(gridElement, element('craft'));
const inventory = setupInventory({ request, createIcon: icon, onSelect: selectItem });
async function request(url, body) {
    let response;
    try {
        response = await fetch(url, body === undefined ? {} : {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } catch {
        throw new Error('Could not reach the game. Check that the server is running and try again.');
    }
    if (!response.ok) {
        if (response.status === 409) {
            challenge = null;
            throw new Error('This recipe is no longer active. Click New recipe.');
        }
        throw new Error('Something went wrong. Try again or get a new recipe.');
    }
    return response.json();
}

function feedback(message, type = '') {
    element('craft-feedback').className = `feedback ${type}`;
    element('craft-feedback').textContent = message;
}

function icon(item) {
    const img = document.createElement('img');
    img.src = item.iconUrl;
    img.alt = '';
    img.draggable = false;
    return img;
}

function refreshGrid() {
    gridElement.classList.toggle('is-solution', showingSolution);
    [...element('craft-grid').children].forEach((button, index) => {
        const item = grid[index];
        button.replaceChildren();
        if (item) button.append(icon(item));
        const label = `Row ${Math.floor(index / 3) + 1}, column ${index % 3 + 1}: ${item?.name ?? 'empty'}`;
        button.setAttribute('aria-label', label);
        button.title = label;
        button.disabled = !canEditGrid();
    });
    element('craft').disabled = !canEditGrid() || grid.every(item => !item);
    element('new-recipe').disabled = working;
    element('new-recipe').textContent = !challenge ? 'New recipe'
        : showingSolution ? 'Next recipe →' : solutionRequested ? 'Retry reveal' : 'Skip recipe';
}

function canEditGrid() {
    return !working && !solved && !solutionRequested && !!challenge;
}

function changeCell(index, item) {
    if (!canEditGrid()) return;
    grid[index] = item;
    refreshGrid();
    feedback('Place your ingredients and click Craft!');
}

function selectItem(item) {
    finishPainting();
    selected = item;
    setHeldItemCursor(item);
    inventory.setSelected(item);
}

async function newChallenge(previousSuccess = null) {
    if (working) return;
    if (!previousSuccess) effects.clear();
    finishPainting();
    working = true;
    solved = false;
    showingSolution = false;
    solutionRequested = false;
    challenge = null;
    grid.fill(null);
    selectItem(null);
    element('target-name').textContent = 'Loading recipe…';
    element('target-icon').hidden = true;
    feedback(previousSuccess ?? 'Loading recipe…', previousSuccess ? 'success' : '');
    refreshGrid();
    try {
        challenge = await request('/api/crafting/challenges', {});
        element('target-name').textContent = challenge.target.name;
        element('target-icon').src = challenge.target.iconUrl;
        element('target-icon').hidden = false;
        feedback(previousSuccess ?? 'Place your ingredients and click Craft!', previousSuccess ? 'success' : '');
    } catch (error) {
        element('target-name').textContent = 'Could not load the recipe';
        feedback(error.message, 'error');
    } finally {
        working = false;
        refreshGrid();
    }
}

async function craft() {
    if (!canEditGrid() || grid.every(item => !item)) return;
    finishPainting();
    working = true;
    let nextRecipeMessage = null;
    refreshGrid();
    feedback('Checking your recipe…');
    try {
        const result = await request('/api/crafting/attempts', {
            challengeId: challenge.id,
            grid: grid.map(item => item?.id ?? null)
        });
        solved = result.correct;
        feedback(result.message, result.correct ? 'success' : 'error');
        effects.play(result.correct);
        if (result.correct) nextRecipeMessage = result.message;
    } catch (error) {
        feedback(error.message, 'error');
    } finally {
        working = false;
        if (!nextRecipeMessage) refreshGrid();
    }
    if (nextRecipeMessage) await newChallenge(nextRecipeMessage);
}

async function skipRecipe() {
    if (working) return;
    effects.clear();
    if (!challenge || showingSolution) {
        await newChallenge();
        return;
    }
    finishPainting();
    working = true;
    solutionRequested = true;
    selectItem(null);
    refreshGrid();
    feedback('Loading the correct recipe…');
    try {
        const solution = await request(`/api/crafting/challenges/${encodeURIComponent(challenge.id)}/solution`, {});
        grid.splice(0, grid.length, ...solution.grid);
        showingSolution = true;
        feedback(solution.message);
    } catch (error) {
        feedback(error.message, 'error');
    } finally {
        working = false;
        refreshGrid();
    }
}

element('craft').addEventListener('click', craft);
element('new-recipe').addEventListener('click', skipRecipe);
refreshGrid();
newChallenge();
