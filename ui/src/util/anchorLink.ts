function hashFor(route: 'test' | 'embed', testId: string, method?: string, invocation?: number): string {
    const params = new URLSearchParams();
    if (method) params.set('method', method);
    if (method && invocation !== undefined) params.set('invocation', String(invocation));
    const query = params.toString();
    return `#/${route}/${testId}${query ? `?${query}` : ''}`;
}

export function anchorHash(testId: string, method?: string, invocation?: number): string {
    return hashFor('test', testId, method, invocation);
}

// Theme is left to the host page (`?theme=` on the embed URL), so the same
// link works in a light and a dark host.
export function embedHash(testId: string, method?: string, invocation?: number): string {
    return hashFor('embed', testId, method, invocation);
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
