import { setupCraftingInput } from './crafting-input.js';

const element = id => document.getElementById(id);
const grid = Array(9).fill(null);
let challenge = null;
let selected = null;
let solved = false;
let working = false;
let showingSolution = false;
let solutionRequested = false;
let page = 0;
let pages = 1;
let searchVersion = 0;
let searchTimer;
const gridElement = element('craft-grid');
const heldIcon = element('held-item');
const { finishPainting, updateHeldIcon } = setupCraftingInput({
    gridElement, heldIcon, canEdit: canEditGrid, getSelected: () => selected, onCellChange: changeCell
});
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
    element('clear-grid').disabled = !canEditGrid() || grid.every(item => !item);
    element('drop-item').disabled = working || !selected;
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
    selected = item;
    element('selected-icon').hidden = !item;
    if (item) element('selected-icon').src = item.iconUrl;
    element('selected-name').textContent = item
        ? `Holding: ${item.name}. Right-click to place; left-click to erase. Drag with either button.`
        : 'Empty hands. Choose an ingredient from the inventory. Left-click still erases.';
    if (item) heldIcon.src = item.iconUrl;
    updateHeldIcon();
    element('drop-item').disabled = working || !item;
    for (const button of element('inventory').children) {
        button.setAttribute('aria-pressed', String(button.dataset.itemId === item?.id));
    }
}

async function loadItems(requestedPage = 0) {
    const version = ++searchVersion;
    element('inventory-status').textContent = 'Searching for ingredients…';
    element('previous-page').disabled = true;
    element('next-page').disabled = true;
    element('retry-items').hidden = true;
    try {
        const result = await request(`/api/items?q=${encodeURIComponent(element('item-search').value)}&page=${requestedPage}`);
        if (version !== searchVersion) return;
        page = result.page;
        pages = result.pages;
        element('inventory').replaceChildren();
        for (const item of result.items) {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'inventory-item';
            button.dataset.itemId = item.id;
            button.title = item.name;
            button.setAttribute('aria-label', item.name);
            button.setAttribute('aria-pressed', String(selected?.id === item.id));
            const name = document.createElement('span');
            name.textContent = item.name;
            button.append(icon(item), name);
            button.addEventListener('click', () => selectItem(item));
            element('inventory').append(button);
        }
        element('inventory').scrollTop = 0;
        element('inventory-status').textContent = result.total
            ? `${result.total.toLocaleString('en-US')} items. Hover over an icon to see its full name.`
            : 'No items found. Try a different name.';
        element('page-label').textContent = `Page ${page + 1} of ${pages}`;
        element('previous-page').disabled = page === 0;
        element('next-page').disabled = page + 1 >= pages;
    } catch (error) {
        if (version !== searchVersion) return;
        element('inventory-status').textContent = error.message;
        element('retry-items').hidden = false;
    }
}

async function newChallenge(previousSuccess = null) {
    if (working) return;
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

element('item-search').addEventListener('input', () => {
    ++searchVersion;
    clearTimeout(searchTimer);
    element('previous-page').disabled = true;
    element('next-page').disabled = true;
    searchTimer = setTimeout(() => loadItems(), 180);
});
element('clear-search').addEventListener('click', () => {
    clearTimeout(searchTimer);
    element('item-search').value = '';
    element('item-search').focus();
    loadItems();
});
element('previous-page').addEventListener('click', () => loadItems(page - 1));
element('next-page').addEventListener('click', () => loadItems(page + 1));
element('retry-items').addEventListener('click', () => loadItems());
element('drop-item').addEventListener('click', () => selectItem(null));
element('clear-grid').addEventListener('click', () => {
    if (!canEditGrid()) return;
    grid.fill(null);
    refreshGrid();
    feedback('The grid is empty. Try a new combination.');
});
element('craft').addEventListener('click', craft);
element('new-recipe').addEventListener('click', skipRecipe);
refreshGrid();
loadItems();
newChallenge();
