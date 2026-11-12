export function formatElapsed(milliseconds) {
    const tenths = Math.max(0, Math.floor(milliseconds / 100));
    const minutes = String(Math.floor(tenths / 600)).padStart(2, '0');
    const seconds = String(Math.floor(tenths / 10) % 60).padStart(2, '0');
    return `${minutes}:${seconds}.${tenths % 10}`;
}

// The server owns the checklist and elapsed time. This module only renders them.
export function setupStackPanel({ onStart, onStop }) {
    const element = id => document.getElementById(id);
    let snapshot = { status: 'INACTIVE', elapsedMillis: 0 };
    let observedAt = performance.now();
    let timer;
    let checklistVersion = '';
    let busy = false;

    function tick() {
        const elapsed = snapshot.elapsedMillis + (snapshot.status === 'RUNNING' ? performance.now() - observedAt : 0);
        const text = formatElapsed(elapsed);
        if (element('stack-timer').textContent !== text) element('stack-timer').textContent = text;
    }

    function setBusy(value) {
        busy = value;
        element('stack-start').disabled = busy || snapshot.status === 'RUNNING';
        element('stack-stop').disabled = busy || snapshot.status === 'INACTIVE';
    }

    function render(state) {
        snapshot = state;
        observedAt = performance.now();
        clearInterval(timer);
        tick();
        if (state.status === 'RUNNING') timer = setInterval(tick, 50);
        element('stack-panel').dataset.status = state.status;
        element('stack-start').textContent = state.status === 'COMPLETE' ? 'Start again' : 'Start The Stack';
        const text = state.status === 'RUNNING'
            ? `${state.remaining} remaining · ${state.completed} / ${state.total} crafted`
            : state.status === 'COMPLETE' ? `Stack complete! ${state.total} / ${state.total} crafted.`
                : '64 targets. One stack. Ready when you are.';
        if (element('stack-status').textContent !== text) element('stack-status').textContent = text;
        element('stack-progress').hidden = state.status === 'INACTIVE';
        element('stack-progress').max = state.total || 64;
        element('stack-progress').value = state.completed || 0;
        const details = element('stack-details');
        details.hidden = state.status === 'INACTIVE';
        if (details.hidden) details.open = false;
        const version = `${state.id}:${state.completed}:${state.status}`;
        if (version !== checklistVersion) {
            checklistVersion = version;
            element('stack-checklist').replaceChildren();
            for (const entry of state.entries || []) {
                const row = document.createElement('li');
                row.dataset.targetId = entry.target.id;
                row.classList.toggle('is-complete', entry.completed);
                row.classList.toggle('is-current', entry.current);
                const check = document.createElement('span');
                check.className = 'stack-check';
                check.textContent = entry.completed ? 'x' : entry.current ? '>' : '';
                check.setAttribute('aria-hidden', 'true');
                const image = document.createElement('img');
                image.src = entry.target.iconUrl;
                image.alt = '';
                image.loading = 'lazy';
                const name = document.createElement('span');
                name.className = 'stack-name';
                name.textContent = entry.target.name;
                row.setAttribute('aria-label', `${entry.target.name}, ${entry.completed ? 'crafted' : entry.current ? 'current' : 'waiting'}`);
                row.append(check, image, name);
                element('stack-checklist').append(row);
            }
        }
        setBusy(busy);
    }

    element('stack-start').addEventListener('click', onStart);
    element('stack-stop').addEventListener('click', onStop);
    render(snapshot);
    return { render, setBusy };
}
