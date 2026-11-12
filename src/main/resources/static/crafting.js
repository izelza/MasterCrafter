import { setupCraftingInput } from './crafting-input.js?v=20261112-2';
import { setHeldItemCursor } from './item-cursor.js';
import { setupInventory } from './inventory.js?v=20261112-5';
import { setupCraftingEffects } from './crafting-effects.js?v=stack-1';
import { setupStackPanel } from './stack.js?v=stack-1';
import { request } from './game-api.js';

const element = id => document.getElementById(id);
const grid = Array(9).fill(null);
let challenge = null;
let selected = null;
let solved = false;
let working = false;
let showingSolution = false;
let solutionRequested = false;
let recoveryNeeded = false;
let stackState = { status: 'INACTIVE' };
const gridElement = element('craft-grid');
const { finishPainting } = setupCraftingInput({
    gridElement, canEdit: canEditGrid, getSelected: () => selected, getCell: index => grid[index], onCellChange: changeCell
});
const effects = setupCraftingEffects(gridElement, element('craft'));
const inventory = setupInventory({ request, createIcon: icon, onSelect: selectItem });
const stackPanel = setupStackPanel({ onStart: () => changeMode('start'), onStop: () => changeMode('stop') });

function feedback(message, type = '') {
    element('craft-feedback').className = `feedback ${type}`;
    element('craft-feedback').textContent = message;
}

function icon(item) {
    const image = document.createElement('img');
    image.src = item.iconUrl;
    image.alt = '';
    image.draggable = false;
    return image;
}

function canEditGrid() {
    return !working && !solved && !solutionRequested && !recoveryNeeded && !!challenge && stackState.status !== 'COMPLETE';
}

function refreshGrid() {
    gridElement.classList.toggle('is-solution', showingSolution);
    [...gridElement.children].forEach((button, index) => {
        const item = grid[index];
        button.replaceChildren();
        if (item) button.append(icon(item));
        const label = `Row ${Math.floor(index / 3) + 1}, column ${index % 3 + 1}: ${item?.name ?? 'empty'}`;
        button.setAttribute('aria-label', label);
        button.title = label;
        button.disabled = !canEditGrid();
    });
    element('craft').disabled = !canEditGrid() || grid.every(item => !item);
    const inStack = stackState.status !== 'INACTIVE';
    element('new-recipe').disabled = working || (inStack && !recoveryNeeded);
    element('new-recipe').title = inStack ? 'Finish this recipe or Stop The Stack to return to free play.' : '';
    element('new-recipe').textContent = recoveryNeeded ? 'Retry connection' : !challenge ? 'New recipe'
        : showingSolution ? 'Next recipe \u2192' : solutionRequested ? 'Retry reveal' : 'Skip recipe';
    stackPanel.setBusy(working);
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

function applyGame(state, previousSuccess = null) {
    if (challenge?.id !== state.challenge?.id) {
        finishPainting();
        grid.fill(null);
        selectItem(null);
    }
    challenge = state.challenge;
    solved = state.status === 'SOLVED';
    showingSolution = state.status === 'REVEALED';
    solutionRequested = showingSolution;
    recoveryNeeded = false;
    stackState = state.stack;
    stackPanel.render(stackState);
    if (challenge) {
        element('target-name').textContent = challenge.target.name;
        element('target-icon').src = challenge.target.iconUrl;
        element('target-icon').hidden = false;
    }
    if (showingSolution) grid.splice(0, grid.length, ...state.solution.grid);
    if (stackState.status === 'COMPLETE') {
        selectItem(null);
        feedback(`The Stack is complete! All ${stackState.total} recipes crafted.`, 'success');
    } else if (showingSolution) {
        feedback(state.solution.message);
    } else {
        feedback(previousSuccess ?? 'Place your ingredients and click Craft!', previousSuccess ? 'success' : '');
    }
}

// Restore the current server session on reload or after a lost HTTP response.
async function playableGame() {
    let state = await request('/api/crafting/game');
    let success = null;
    if (!state.challenge || (state.status === 'SOLVED' && state.stack.status !== 'COMPLETE')) {
        if (state.challenge) success = `Correct! You crafted ${state.challenge.target.name}.`;
        await request('/api/crafting/challenges', {});
        state = await request('/api/crafting/game');
    }
    return { state, success };
}

async function recoverGame() {
    try {
        const { state, success } = await playableGame();
        const justCompleted = stackState.status === 'RUNNING' && state.stack.status === 'COMPLETE';
        applyGame(state, success);
        if (justCompleted) effects.celebrateStack();
        else if (success) effects.play(true);
        else if (!showingSolution && stackState.status !== 'COMPLETE') feedback('Your game was restored. Continue crafting.');
    } catch {
        recoveryNeeded = true;
        feedback('Could not reach the game. Click Retry connection or reload the page.', 'error');
    }
}

async function restoreGame() {
    if (working) return;
    working = true;
    refreshGrid();
    try {
        const { state, success } = await playableGame();
        applyGame(state, success);
    } catch {
        recoveryNeeded = true;
        feedback('Could not load the game. Click Retry connection to try again.', 'error');
    } finally {
        working = false;
        refreshGrid();
    }
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
    element('target-name').textContent = 'Loading recipe\u2026';
    element('target-icon').hidden = true;
    feedback(previousSuccess ?? 'Loading recipe\u2026', previousSuccess ? 'success' : '');
    refreshGrid();
    try {
        await request('/api/crafting/challenges', {});
        applyGame(await request('/api/crafting/game'), previousSuccess);
    } catch {
        await recoverGame();
    } finally {
        working = false;
        refreshGrid();
    }
}

async function craft() {
    if (!canEditGrid() || grid.every(item => !item)) return;
    finishPainting();
    working = true;
    let nextMessage = null;
    refreshGrid();
    feedback('Checking your recipe\u2026');
    try {
        const result = await request('/api/crafting/attempts', {
            challengeId: challenge.id, grid: grid.map(item => item?.id ?? null)
        });
        solved = result.correct;
        feedback(result.message, result.correct ? 'success' : 'error');
        if (result.correct && stackState.status === 'RUNNING') {
            const state = await request('/api/crafting/game');
            applyGame(state, result.message);
            if (state.stack.status === 'COMPLETE') effects.celebrateStack();
            else {
                effects.play(true);
                nextMessage = result.message;
            }
        } else {
            effects.play(result.correct);
            if (result.correct) nextMessage = result.message;
        }
    } catch {
        await recoverGame();
    } finally {
        working = false;
        refreshGrid();
    }
    if (nextMessage) await newChallenge(nextMessage);
}

async function skipRecipe() {
    if (working) return;
    if (recoveryNeeded) return restoreGame();
    if (stackState.status !== 'INACTIVE') return;
    effects.clear();
    if (!challenge || showingSolution) return newChallenge();
    finishPainting();
    working = true;
    solutionRequested = true;
    selectItem(null);
    refreshGrid();
    feedback('Loading the correct recipe\u2026');
    try {
        await request(`/api/crafting/challenges/${encodeURIComponent(challenge.id)}/solution`, {});
        applyGame(await request('/api/crafting/game'));
    } catch {
        await recoverGame();
    } finally {
        working = false;
        refreshGrid();
    }
}

async function changeMode(action) {
    if (working) return;
    working = true;
    finishPainting();
    selectItem(null);
    effects.clear();
    refreshGrid();
    feedback(action === 'start' ? 'Building The Stack\u2026' : 'Returning to free play\u2026');
    try {
        const state = await request(`/api/crafting/stack/${action}`, {});
        applyGame(state, action === 'start' ? 'The Stack has started. Craft every target!' : null);
    } catch {
        await recoverGame();
    } finally {
        working = false;
        refreshGrid();
    }
}

element('craft').addEventListener('click', craft);
element('new-recipe').addEventListener('click', skipRecipe);
document.addEventListener('visibilitychange', () => {
    if (!document.hidden && stackState.status === 'RUNNING' && !working) restoreGame();
});
refreshGrid();
restoreGame();