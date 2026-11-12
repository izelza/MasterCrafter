import { setupItemTooltip } from './item-tooltip.js';

// Searching, paging and ingredient selection; crafting rules stay on the server.
export function setupInventory({ request, createIcon, onSelect }) {
    const element = id => document.getElementById(id);
    const inventory = element('inventory');
    const tooltip = setupItemTooltip(inventory);
    let selected = null;
    let page = 0;
    let pages = 1;
    let searchVersion = 0;
    let searchTimer;
    let items = new Map();

    function setSelected(item) {
        selected = item;
        tooltip.hide();
        for (const button of inventory.children) {
            button.setAttribute('aria-pressed', String(button.dataset.itemId === item?.id));
        }
    }

    function choose(event) {
        const button = event.target.closest('.inventory-item');
        if (!button) return;
        event.preventDefault();
        tooltip.hide();
        onSelect(items.get(button.dataset.itemId));
    }
    inventory.addEventListener('click', choose);
    inventory.addEventListener('contextmenu', choose);
    // Suppress the browser menu even if the pointer is between inventory slots.
    inventory.addEventListener('contextmenu', event => event.preventDefault());
    inventory.addEventListener('dragstart', event => event.preventDefault());

    async function loadItems(requestedPage = 0) {
        const version = ++searchVersion;
        tooltip.hide();
        element('inventory-status').textContent = 'Searching for ingredients…';
        element('previous-page').disabled = true;
        element('next-page').disabled = true;
        element('retry-items').hidden = true;
        try {
            const result = await request(`/api/items?q=${encodeURIComponent(element('item-search').value)}&page=${requestedPage}`);
            if (version !== searchVersion) return;
            page = result.page;
            pages = result.pages;
            items = new Map(result.items.map(item => [item.id, item]));
            inventory.replaceChildren();
            for (const item of result.items) {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'inventory-item';
                button.dataset.itemId = item.id;
                button.setAttribute('aria-label', item.name);
                button.setAttribute('aria-pressed', String(selected?.id === item.id));
                button.append(createIcon(item));
                inventory.append(button);
            }
            inventory.scrollTop = 0;
            element('inventory-status').textContent = result.total
                ? `${result.total.toLocaleString('en-US')} items. Hover over an icon to see its name.`
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

    element('item-search').addEventListener('input', () => {
        ++searchVersion;
        tooltip.hide();
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
    loadItems();
    return { setSelected };
}
