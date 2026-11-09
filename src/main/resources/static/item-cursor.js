// A native cursor moves with the pointer without waiting for JavaScript or page repainting.
const cursors = new Map();
let selectionVersion = 0;

async function createCursor(iconUrl) {
    const image = new Image();
    image.src = iconUrl;
    await image.decode();
    const canvas = document.createElement('canvas');
    canvas.width = 32;
    canvas.height = 32;
    const context = canvas.getContext('2d');
    context.imageSmoothingEnabled = false;
    context.drawImage(image, 0, 0, 32, 32);
    return `url("${canvas.toDataURL('image/png')}") 16 16, auto`;
}

export function setHeldItemCursor(item) {
    const version = ++selectionVersion;
    document.body.classList.remove('holding-item');
    document.body.style.removeProperty('--held-item-cursor');
    if (!item) return;

    if (!cursors.has(item.iconUrl)) cursors.set(item.iconUrl, createCursor(item.iconUrl));
    cursors.get(item.iconUrl).then(cursor => {
        // Dropping/changing the item while its image loads must not restore an old cursor.
        if (version !== selectionVersion) return;
        document.body.style.setProperty('--held-item-cursor', cursor);
        document.body.classList.add('holding-item');
    }).catch(() => {
        cursors.delete(item.iconUrl);
    });
}
