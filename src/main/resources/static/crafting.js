const element = id => document.getElementById(id);
const grid = Array(9).fill(null);
let challenge = null;
let selected = null;
let solved = false;
let working = false;
let page = 0;
let pages = 1;
let searchVersion = 0;
let searchTimer;
let paintGesture = null;
const gridElement = element('craft-grid');
const heldIcon = element('held-item');
let pointerPosition = null;

function updateHeldIcon() {
    heldIcon.hidden = !selected || !pointerPosition;
    if (heldIcon.hidden) return;
    heldIcon.style.left = `${pointerPosition.x}px`;
    heldIcon.style.top = `${pointerPosition.y}px`;
}

function trackPointer(event) {
    if (event.pointerType !== 'mouse') return;
    pointerPosition = event.clientX >= 0 && event.clientX < window.innerWidth
        && event.clientY >= 0 && event.clientY < window.innerHeight
        ? { x: event.clientX, y: event.clientY } : null;
    updateHeldIcon();
}

document.addEventListener('pointermove', trackPointer, { capture: true, passive: true });
document.addEventListener('pointerdown', trackPointer, { capture: true, passive: true });
document.documentElement.addEventListener('pointerleave', () => {
    pointerPosition = null;
    updateHeldIcon();
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
    [...element('craft-grid').children].forEach((button, index) => {
        const item = grid[index];
        button.replaceChildren();
        if (item) button.append(icon(item));
        const label = `Row ${Math.floor(index / 3) + 1}, column ${index % 3 + 1}: ${item?.name ?? 'empty'}`;
        button.setAttribute('aria-label', label);
        button.title = label;
        button.disabled = working || solved || !challenge;
    });
    element('craft').disabled = working || solved || !challenge || grid.every(item => !item);
    element('clear-grid').disabled = working || solved || !challenge || grid.every(item => !item);
    element('drop-item').disabled = working || !selected;
    element('new-recipe').disabled = working;
    element('new-recipe').textContent = !challenge ? 'New recipe' : 'Skip recipe';
}

function changeCell(index, item) {
    if (working || solved || !challenge) return;
    grid[index] = item;
    refreshGrid();
    feedback('Place your ingredients and click Craft!');
}

for (let index = 0; index < 9; index++) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'grid-cell';
    button.dataset.slot = index;
    // Mouse actions use pointer events; retain placement by keyboard or touch.
    button.addEventListener('click', event => {
        if (event.detail === 0 || (event.pointerType && event.pointerType !== 'mouse')) {
            changeCell(index, selected);
        }
    });
    button.addEventListener('keydown', event => {
        if (event.key === 'Delete' || event.key === 'Backspace') {
            event.preventDefault();
            changeCell(index, null);
        }
    });
    gridElement.append(button);
}

function finishPainting() {
    if (!paintGesture) return;
    const pointerId = paintGesture.pointerId;
    paintGesture = null;
    if (gridElement.hasPointerCapture(pointerId)) gridElement.releasePointerCapture(pointerId);
}

function paintAt(x, y) {
    const slot = document.elementFromPoint(x, y)?.closest('.grid-cell');
    if (!slot || !gridElement.contains(slot)) return;
    const index = Number(slot.dataset.slot);
    if (paintGesture.visited.has(index)) return;
    paintGesture.visited.add(index);
    changeCell(index, paintGesture.item);
}

function paintTo(x, y) {
    const fromX = paintGesture.x;
    const fromY = paintGesture.y;
    const dx = x - fromX;
    const dy = y - fromY;
    // Sample the path so fast movements still fill every crossed slot.
    const steps = Math.max(1, Math.ceil(Math.hypot(dx, dy) / 12));
    for (let step = 1; step <= steps; step++) {
        paintAt(fromX + dx * step / steps, fromY + dy * step / steps);
    }
    paintGesture.x = x;
    paintGesture.y = y;
}

gridElement.addEventListener('contextmenu', event => event.preventDefault());
gridElement.addEventListener('pointerdown', event => {
    if (event.pointerType !== 'mouse' || ![0, 2].includes(event.button) || working || solved || !challenge) return;
    event.preventDefault();
    finishPainting();
    if (event.button === 2 && !selected) return;
    paintGesture = {
        pointerId: event.pointerId, item: event.button === 0 ? null : selected,
        buttonMask: event.button === 0 ? 1 : 2, visited: new Set(),
        x: event.clientX, y: event.clientY
    };
    gridElement.setPointerCapture(event.pointerId);
    paintAt(event.clientX, event.clientY);
});
gridElement.addEventListener('pointermove', event => {
    if (!paintGesture || paintGesture.pointerId !== event.pointerId) return;
    if (!(event.buttons & paintGesture.buttonMask)) {
        finishPainting();
        return;
    }
    event.preventDefault();
    paintTo(event.clientX, event.clientY);
});
gridElement.addEventListener('pointerup', event => {
    if (paintGesture?.pointerId === event.pointerId && !(event.buttons & paintGesture.buttonMask)) {
        paintTo(event.clientX, event.clientY);
        finishPainting();
    }
});
gridElement.addEventListener('pointercancel', finishPainting);
gridElement.addEventListener('lostpointercapture', finishPainting);
window.addEventListener('blur', () => {
    finishPainting();
    pointerPosition = null;
    updateHeldIcon();
});

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
    if (working || solved || !challenge || grid.every(item => !item)) return;
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
    if (working || solved || !challenge) return;
    grid.fill(null);
    refreshGrid();
    feedback('The grid is empty. Try a new combination.');
});
element('craft').addEventListener('click', craft);
element('new-recipe').addEventListener('click', () => newChallenge());
refreshGrid();
loadItems();
newChallenge();
