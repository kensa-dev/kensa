export function anchorHash(testId: string, method?: string, invocation?: number): string {
    const params = new URLSearchParams();
    if (method) params.set('method', method);
    if (method && invocation !== undefined) params.set('invocation', String(invocation));
    const query = params.toString();
    return `#/test/${testId}${query ? `?${query}` : ''}`;
}

interface ClipboardLike {
    writeText(text: string): Promise<void>;
}

export async function copyLink(
    url: string,
    clipboard: ClipboardLike | undefined = typeof navigator !== 'undefined' ? navigator.clipboard : undefined,
    warn: (message: string, url: string) => void = console.warn,
): Promise<boolean> {
    if (clipboard) {
        try {
            await clipboard.writeText(url);
            return true;
        } catch {
            // fall through to the warn below
        }
    }
    warn('Kensa: clipboard unavailable (needs a secure context: HTTPS or localhost). Copy manually:', url);
    return false;
}
