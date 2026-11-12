// Shared HTTP transport. Game state and validation belong to the backend.
export async function request(url, body) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 12000);
    try {
        const response = await fetch(url, {
            signal: controller.signal,
            cache: 'no-store',
            ...(body === undefined ? {} : {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
            })
        });
        if (!response.ok) {
            const error = new Error(response.status === 409
                ? 'The game has changed. Your current recipe will be restored.'
                : 'Could not complete that action. Try again.');
            error.status = response.status;
            throw error;
        }
        return await response.json();
    } catch (error) {
        if (error.status) throw error;
        throw new Error('Could not reach the game. Check your connection and try again.');
    } finally {
        clearTimeout(timeout);
    }
}
