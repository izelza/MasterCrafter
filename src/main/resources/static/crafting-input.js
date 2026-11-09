// Cursor display and mouse/keyboard/touch input. Game flow stays in crafting.js.
export function setupCraftingInput({ gridElement, heldIcon, canEdit, getSelected, onCellChange }) {
    let paintGesture = null;
    let pointerPosition = null;

    function updateHeldIcon() {
        const selected = getSelected();
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

    for (let index = 0; index < 9; index++) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'grid-cell';
        button.dataset.slot = index;
        // Mouse actions use pointer events; retain placement by keyboard or touch.
        button.addEventListener('click', event => {
            if (event.detail === 0 || (event.pointerType && event.pointerType !== 'mouse')) {
                onCellChange(index, getSelected());
            }
        });
        button.addEventListener('keydown', event => {
            if (event.key === 'Delete' || event.key === 'Backspace') {
                event.preventDefault();
                onCellChange(index, null);
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
        onCellChange(index, paintGesture.item);
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
        if (event.pointerType !== 'mouse' || ![0, 2].includes(event.button) || !canEdit()) return;
        event.preventDefault();
        finishPainting();
        if (event.button === 2 && !getSelected()) return;
        paintGesture = {
            pointerId: event.pointerId, item: event.button === 0 ? null : getSelected(),
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

    return { finishPainting, updateHeldIcon };
}
