export function setupDebugMenu({ onAction, onOpen, onSelectTarget }) {
    const menu = document.getElementById('debug-menu');
    const success = document.getElementById('debug-success');
    const failure = document.getElementById('debug-failure');
    const complete = document.getElementById('debug-complete');
    const target = document.getElementById('debug-target');
    let positioned = false;
    let drag = null;
    let returnFocus = null;

    function position(left, top) {
        menu.style.left = `${left}px`;
        menu.style.top = `${top}px`;
    }

    function finishDrag() {
        const pointerId = drag?.pointerId;
        drag = null;
        menu.classList.remove('is-dragging');
        if (pointerId !== undefined && menu.hasPointerCapture(pointerId)) menu.releasePointerCapture(pointerId);
    }

    menu.addEventListener('pointerdown', event => {
        if (event.button !== 0 || event.target.closest('button') || drag) return;
        event.preventDefault();
        const bounds = menu.getBoundingClientRect();
        drag = { pointerId: event.pointerId, x: event.clientX - bounds.left, y: event.clientY - bounds.top };
        menu.setPointerCapture(event.pointerId);
        menu.classList.add('is-dragging');
    });
    menu.addEventListener('pointermove', event => {
        if (!drag || event.pointerId !== drag.pointerId) return;
        if (!(event.buttons & 1)) return finishDrag();
        // The pointer stops at the viewport edge; the panel may extend beyond it.
        const x = Math.max(0, Math.min(event.clientX, document.documentElement.clientWidth));
        const y = Math.max(0, Math.min(event.clientY, document.documentElement.clientHeight));
        position(x + scrollX - drag.x, y + scrollY - drag.y);
    });
    for (const type of ['pointerup', 'pointercancel', 'lostpointercapture']) {
        menu.addEventListener(type, event => {
            if (event.pointerId === drag?.pointerId) finishDrag();
        });
    }
    window.addEventListener('blur', finishDrag);
    window.addEventListener('resize', finishDrag);

    function close() {
        finishDrag();
        menu.hidden = true;
        returnFocus?.focus({ preventScroll: true });
    }

    function open() {
        returnFocus = document.activeElement;
        onOpen();
        menu.hidden = false;
        const bounds = menu.getBoundingClientRect();
        if (!positioned || bounds.right <= 0 || bounds.left >= innerWidth || bounds.bottom <= 0 || bounds.top >= innerHeight) {
            position(scrollX + 8, scrollY + 8);
        }
        positioned = true;
    }
    document.getElementById('debug-close').addEventListener('click', close);
    for (const [button, action] of [[success, 'SUCCESS'], [failure, 'FAILURE'], [complete, 'COMPLETE_STACK']]) {
        button.addEventListener('click', () => {
            onAction(action);
        });
    }
    target.addEventListener('click', () => {
        onSelectTarget();
    });
    return {
        open,
        render({ canAttempt, canComplete, canSelectTarget, selectingTarget }) {
            success.disabled = failure.disabled = !canAttempt;
            complete.disabled = !canComplete;
            complete.title = canComplete ? 'Finish the running Stack and show its celebration.' : 'Start The Stack first.';
            target.disabled = !canSelectTarget;
            target.textContent = selectingTarget ? 'Cancel target selection' : 'Select target item';
            target.title = canSelectTarget ? 'Choose the next target by clicking an inventory item.' : 'Stop The Stack before choosing a target.';
        }
    };
}
