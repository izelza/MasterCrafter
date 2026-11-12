// Small, local feedback effects. Game flow never waits for an animation.
export function setupCraftingEffects(grid, craftButton) {
    const layer = document.createElement('span');
    layer.className = 'craft-effect-layer';
    layer.setAttribute('aria-hidden', 'true');
    grid.parentElement.append(layer);
    const reducedMotion = matchMedia('(prefers-reduced-motion: reduce)');
    let cleanupTimer;
    let celebration;
    let celebrationTimer;

    function clear() {
        clearTimeout(cleanupTimer);
        clearTimeout(celebrationTimer);
        celebration?.remove();
        celebration = null;
        layer.replaceChildren();
        delete layer.dataset.result;
        grid.classList.remove('is-mistake');
        if (layer.parentElement !== grid.parentElement) grid.parentElement.append(layer);
    }

    function celebrateStack() {
        clear();
        if (reducedMotion.matches || document.hidden) return;
        celebration = document.createElement('div');
        celebration.className = 'stack-celebration';
        celebration.setAttribute('aria-hidden', 'true');
        const colors = ['#80d65b', '#f4d56b', '#c4ef95', '#fff2aa'];
        for (let burst = 0; burst < 3; burst++) {
            const center = .25 + burst * .25;
            const spread = Math.min(170, innerWidth * center - 12, innerWidth * (1 - center) - 12);
            for (let index = 0; index < 24; index++) {
                const spark = document.createElement('i');
                spark.className = 'craft-spark stack-spark';
                const angle = index / 24 * Math.PI * 2;
                const distance = .7 + Math.random() * .3;
                spark.style.left = `${center * 100}%`;
                spark.style.top = burst === 1 ? '32%' : '42%';
                spark.style.setProperty('--spark-x', `${Math.cos(angle) * spread * distance}px`);
                spark.style.setProperty('--spark-y', `${Math.sin(angle) * 150 * distance + 70}px`);
                spark.style.setProperty('--spark-color', colors[index % colors.length]);
                spark.style.setProperty('--spark-size', index % 3 === 0 ? '8px' : '5px');
                spark.style.animationDelay = `${burst * 280 + index % 3 * 20}ms`;
                celebration.append(spark);
            }
        }
        document.body.append(celebration);
        celebrationTimer = setTimeout(() => {
            celebration?.remove();
            celebration = null;
        }, 2200);
    }

    function play(correct) {
        clear();
        if (reducedMotion.matches || document.hidden) return;
        layer.dataset.result = correct ? 'correct' : 'incorrect';
        if (correct) {
            craftButton.append(layer);
            const buttonRect = craftButton.getBoundingClientRect();
            const centerX = buttonRect.left + buttonRect.width / 2;
            const spreadX = Math.min(buttonRect.width / 2 + 44, centerX - 8, innerWidth - centerX - 8);
            const spreadY = buttonRect.height / 2 + 36;
            const colors = ['#80d65b', '#f4d56b', '#c4ef95'];
            for (let index = 0; index < 18; index++) {
                const spark = document.createElement('i');
                spark.className = 'craft-spark';
                const angle = index / 18 * Math.PI * 2;
                const distance = .85 + Math.random() * .15;
                spark.style.setProperty('--spark-x', `${Math.cos(angle) * spreadX * distance}px`);
                spark.style.setProperty('--spark-y', `${Math.sin(angle) * spreadY * distance}px`);
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
    window.addEventListener('resize', clear);
    document.addEventListener('visibilitychange', () => { if (document.hidden) clear(); });
    return { play, clear, celebrateStack };
}
