// True when the report runs inside another page's frame.
export function isEmbedded(): boolean {
    return typeof window !== 'undefined' && window.parent !== window;
}
