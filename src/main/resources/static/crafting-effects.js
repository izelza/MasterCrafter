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
        const bursts = [
            { x: .5, y: .42, delay: 0 },
            { x: .22, y: .3, delay: 220 },
            { x: .78, y: .34, delay: 400 },
            { x: .35, y: .56, delay: 660 },
            { x: .65, y: .5, delay: 820 },
            { x: .5, y: .32, delay: 1000, finale: true }
        ];
        for (const burst of bursts) {
            const count = burst.finale ? 64 : 48;
            const spreadX = Math.min(burst.finale ? 360 : 300, innerWidth * burst.x - 16, innerWidth * (1 - burst.x) - 16);
            const spreadY = Math.min(burst.finale ? 300 : 240, innerHeight * .3);
            for (let index = 0; index < count; index++) {
                const spark = document.createElement('i');
                spark.className = 'craft-spark stack-spark';
                const angle = index / count * Math.PI * 2;
                // Two uneven rings give each explosion a dense core and a wide outer burst.
                const distance = (index % 2 === 0 ? .85 : .5) + Math.random() * .15;
                spark.style.left = `${burst.x * 100}%`;
                spark.style.top = `${burst.y * 100}%`;
                spark.style.setProperty('--spark-x', `${Math.cos(angle) * spreadX * distance}px`);
                spark.style.setProperty('--spark-y', `${Math.sin(angle) * spreadY * distance}px`);
                spark.style.setProperty('--spark-color', colors[index % colors.length]);
                spark.style.setProperty('--spark-size', index % 3 === 0 ? '12px' : '8px');
                spark.style.animationDelay = `${burst.delay + index % 3 * 20}ms`;
                celebration.append(spark);
            }
        }
        document.body.append(celebration);
        celebrationTimer = setTimeout(() => {
            celebration?.remove();
            celebration = null;
        }, 3200);
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
