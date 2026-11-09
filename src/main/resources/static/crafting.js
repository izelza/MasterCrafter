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

async function request(url, body) {
    let response;
    try {
        response = await fetch(url, body === undefined ? {} : {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } catch {
        throw new Error('Kunde inte nå spelet. Kontrollera att servern körs och försök igen.');
    }
    if (!response.ok) {
        if (response.status === 409) {
            challenge = null;
            throw new Error('Receptet är inte längre aktivt. Klicka på Nytt recept.');
        }
        throw new Error('Något gick fel. Prova igen eller hämta ett nytt recept.');
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
    return img;
}

function refreshGrid() {
    [...element('craft-grid').children].forEach((button, index) => {
        const item = grid[index];
        button.replaceChildren();
        if (item) button.append(icon(item));
        const label = `Rad ${Math.floor(index / 3) + 1}, kolumn ${index % 3 + 1}: ${item?.name ?? 'tom'}`;
        button.setAttribute('aria-label', label);
        button.title = label;
        button.disabled = working || solved || !challenge;
    });
    element('craft').disabled = working || solved || !challenge || grid.every(item => !item);
    element('clear-grid').disabled = working || solved || !challenge || grid.every(item => !item);
    element('eraser').disabled = working || solved || !challenge;
    element('new-recipe').disabled = working;
    element('new-recipe').textContent = !challenge ? 'Nytt recept' : solved ? 'Nästa recept →' : 'Hoppa över';
}

function changeCell(index, item) {
    if (working || solved || !challenge) return;
    grid[index] = item;
    refreshGrid();
    feedback('Placera ingredienserna och klicka på Crafta!');
}

for (let index = 0; index < 9; index++) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'grid-cell';
    button.addEventListener('click', () => changeCell(index, selected));
    button.addEventListener('contextmenu', event => {
        event.preventDefault();
        changeCell(index, null);
    });
    button.addEventListener('keydown', event => {
        if (event.key === 'Delete' || event.key === 'Backspace') {
            event.preventDefault();
            changeCell(index, null);
        }
    });
    element('craft-grid').append(button);
}

function selectItem(item) {
    selected = item;
    element('selected-icon').hidden = !item;
    if (item) element('selected-icon').src = item.iconUrl;
    element('selected-name').textContent = item
        ? `Vald: ${item.name}. Klicka i griden för att placera.`
        : 'Sudda är valt. Klicka på en ruta för att tömma den.';
    element('eraser').setAttribute('aria-pressed', String(!item));
    for (const button of element('inventory').children) {
        button.setAttribute('aria-pressed', String(button.dataset.itemId === item?.id));
    }
}

async function loadItems(requestedPage = 0) {
    const version = ++searchVersion;
    element('inventory-status').textContent = 'Söker ingredienser…';
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
            ? `${result.total.toLocaleString('sv-SE')} föremål. Håll musen över en ikon för att se hela namnet.`
            : 'Inga föremål hittades. Prova ett annat namn.';
        element('page-label').textContent = `Sida ${page + 1} av ${pages}`;
        element('previous-page').disabled = page === 0;
        element('next-page').disabled = page + 1 >= pages;
    } catch (error) {
        if (version !== searchVersion) return;
        element('inventory-status').textContent = error.message;
        element('retry-items').hidden = false;
    }
}

async function newChallenge() {
    if (working) return;
    working = true;
    solved = false;
    challenge = null;
    grid.fill(null);
    element('target-name').textContent = 'Hämtar recept…';
    element('target-icon').hidden = true;
    feedback('Hämtar recept…');
    refreshGrid();
    try {
        challenge = await request('/api/crafting/challenges', {});
        element('target-name').textContent = challenge.target.name;
        element('target-icon').src = challenge.target.iconUrl;
        element('target-icon').hidden = false;
        feedback('Placera ingredienserna och klicka på Crafta!');
    } catch (error) {
        element('target-name').textContent = 'Receptet kunde inte hämtas';
        feedback(error.message, 'error');
    } finally {
        working = false;
        refreshGrid();
    }
}

async function craft() {
    if (working || solved || !challenge || grid.every(item => !item)) return;
    working = true;
    refreshGrid();
    feedback('Kontrollerar receptet…');
    try {
        const result = await request('/api/crafting/attempts', {
            challengeId: challenge.id,
            grid: grid.map(item => item?.id ?? null)
        });
        solved = result.correct;
        feedback(result.message, result.correct ? 'success' : 'error');
    } catch (error) {
        feedback(error.message, 'error');
    } finally {
        working = false;
        refreshGrid();
        if (solved) element('new-recipe').focus({ preventScroll: true });
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
element('eraser').addEventListener('click', () => selectItem(null));
element('clear-grid').addEventListener('click', () => {
    if (working || solved || !challenge) return;
    grid.fill(null);
    refreshGrid();
    feedback('Griden är tom. Prova en ny kombination.');
});
element('craft').addEventListener('click', craft);
element('new-recipe').addEventListener('click', newChallenge);
refreshGrid();
loadItems();
newChallenge();
