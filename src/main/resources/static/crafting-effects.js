// Small, local feedback effects. Game flow never waits for an animation.
export function setupCraftingEffects(grid) {
    const layer = document.createElement('div');
    layer.className = 'craft-effect-layer';
    layer.setAttribute('aria-hidden', 'true');
    grid.parentElement.append(layer);
    const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
    let cleanupTimer;

    function clear() {
        clearTimeout(cleanupTimer);
        layer.replaceChildren();
        delete layer.dataset.result;
        grid.classList.remove('is-mistake');
    }

    function play(correct) {
        clear();
        if (reducedMotion.matches || document.hidden) return;
        layer.dataset.result = correct ? 'correct' : 'incorrect';
        if (correct) {
            const colors = ['#80d65b', '#f4d56b', '#c4ef95'];
            for (let index = 0; index < 18; index++) {
                const spark = document.createElement('i');
                spark.className = 'craft-spark';
                const angle = index / 18 * Math.PI * 2;
                const radius = 45 + Math.random() * 30;
                spark.style.setProperty('--spark-x', `${Math.cos(angle) * radius}px`);
                spark.style.setProperty('--spark-y', `${Math.sin(angle) * radius + 12}px`);
                spark.style.setProperty('--spark-color', colors[index % colors.length]);
                spark.style.setProperty('--spark-size', index % 3 === 0 ? '6px' : '4px');
                spark.style.animationDelay = `${index % 3 * 20}ms`;
                layer.append(spark);
            }
        } else {
            // Restart a previous nudge if another incorrect attempt arrives quickly.
            void grid.offsetWidth;
            grid.classList.add('is-mistake');
        }
        cleanupTimer = setTimeout(clear, correct ? 600 : 240);
    }

    reducedMotion.addEventListener('change', clear);
    document.addEventListener('visibilitychange', () => { if (document.hidden) clear(); });
    return { play, clear };
}
