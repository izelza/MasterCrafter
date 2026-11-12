export function setupDebugMenu({ onAction, onOpen, onSelectTarget }) {
    const tools = document.getElementById('debug-tools');
    const toggle = document.getElementById('debug-toggle');
    const menu = document.getElementById('debug-menu');
    const success = document.getElementById('debug-success');
    const failure = document.getElementById('debug-failure');
    const complete = document.getElementById('debug-complete');
    const target = document.getElementById('debug-target');
    let positioned = false;
    let drag = null;

    function position(left, top) {
        const bounds = menu.getBoundingClientRect();
        menu.style.left = `${Math.max(8, Math.min(left, document.documentElement.clientWidth - bounds.width - 8))}px`;
        menu.style.top = `${Math.max(8, Math.min(top, document.documentElement.clientHeight - bounds.height - 8))}px`;
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
        position(event.clientX - drag.x, event.clientY - drag.y);
    });
    for (const type of ['pointerup', 'pointercancel', 'lostpointercapture']) {
        menu.addEventListener(type, event => {
            if (event.pointerId === drag?.pointerId) finishDrag();
        });
    }
    window.addEventListener('blur', finishDrag);
    window.addEventListener('resize', () => {
        finishDrag();
        if (!menu.hidden) {
            const bounds = menu.getBoundingClientRect();
            position(bounds.left, bounds.top);
        }
    });

    function close(restoreFocus = false) {
        finishDrag();
        menu.hidden = true;
        toggle.setAttribute('aria-expanded', 'false');
        if (restoreFocus) toggle.focus({ preventScroll: true });
    }

    toggle.addEventListener('click', () => {
        if (!menu.hidden) return close();
        onOpen();
        menu.hidden = false;
        const bounds = positioned ? menu.getBoundingClientRect() : toggle.getBoundingClientRect();
        position(bounds.left, positioned ? bounds.top : bounds.bottom + 8);
        positioned = true;
        toggle.setAttribute('aria-expanded', 'true');
    });
    document.getElementById('debug-close').addEventListener('click', () => close(true));
    document.addEventListener('pointerdown', event => {
        if (!tools.contains(event.target)) close();
    });
    document.addEventListener('focusin', event => {
        if (!tools.contains(event.target)) close();
    });
    tools.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !menu.hidden) {
            event.preventDefault();
            close(true);
        }
    });
    for (const [button, action] of [[success, 'SUCCESS'], [failure, 'FAILURE'], [complete, 'COMPLETE_STACK']]) {
        button.addEventListener('click', () => {
            close(true);
            onAction(action);
        });
    }
    target.addEventListener('click', () => {
        close(true);
        onSelectTarget();
    });
    return {
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
