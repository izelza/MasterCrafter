// One overlay outside the scrolling inventory, so names never affect slot layout.
export function setupItemTooltip(inventory) {
    const tooltip = document.createElement('div');
    tooltip.id = 'item-tooltip';
    tooltip.className = 'item-tooltip';
    tooltip.role = 'tooltip';
    tooltip.hidden = true;
    const label = document.createElement('span');
    tooltip.append(label);
    document.body.append(tooltip);
    let activeSlot = null;

    function hide() {
        if (!activeSlot && tooltip.hidden) return;
        activeSlot?.removeAttribute('aria-describedby');
        activeSlot = null;
        tooltip.hidden = true;
    }

    function show(slot, x, y) {
        if (activeSlot !== slot) {
            hide();
            activeSlot = slot;
            label.textContent = slot.getAttribute('aria-label');
            slot.setAttribute('aria-describedby', tooltip.id);
        }
        tooltip.hidden = false;
        const { width, height } = tooltip.getBoundingClientRect();
        let left = x + 18;
        if (left + width > window.innerWidth - 8) left = x - width - 18;
        tooltip.style.left = `${Math.max(8, Math.min(left, window.innerWidth - width - 8))}px`;
        tooltip.style.top = `${Math.max(8, Math.min(y - 16, window.innerHeight - height - 8))}px`;
    }

    inventory.addEventListener('pointermove', event => {
        const slot = event.target.closest('.inventory-item');
        if (!slot || event.buttons || event.pointerType === 'touch') return hide();
        show(slot, event.clientX, event.clientY);
    });
    inventory.addEventListener('pointerleave', hide);
    inventory.addEventListener('pointerdown', hide);
    inventory.addEventListener('focusin', event => {
        const slot = event.target.closest('.inventory-item');
        if (slot?.matches(':focus-visible')) {
            const rect = slot.getBoundingClientRect();
            show(slot, rect.right, rect.top);
        }
    });
    inventory.addEventListener('focusout', hide);
    document.addEventListener('keydown', event => { if (event.key === 'Escape') hide(); });
    window.addEventListener('scroll', hide, true);
    window.addEventListener('resize', hide);
    window.addEventListener('blur', hide);
    return { hide };
}
