export function setupDebugMenu({ onAction, onOpen, onSelectTarget }) {
    const tools = document.getElementById('debug-tools');
    const toggle = document.getElementById('debug-toggle');
    const menu = document.getElementById('debug-menu');
    const success = document.getElementById('debug-success');
    const failure = document.getElementById('debug-failure');
    const complete = document.getElementById('debug-complete');
    const target = document.getElementById('debug-target');

    function close(restoreFocus = false) {
        menu.hidden = true;
        toggle.setAttribute('aria-expanded', 'false');
        if (restoreFocus) toggle.focus({ preventScroll: true });
    }

    toggle.addEventListener('click', () => {
        if (!menu.hidden) return close();
        onOpen();
        menu.hidden = false;
        toggle.setAttribute('aria-expanded', 'true');
    });
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
