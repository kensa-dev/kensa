import {EmbedTheme} from '@/util/embedRoute';

// True means dark. `auto` follows the OS via prefers-color-scheme; an explicit
// value wins. Nothing here touches localStorage: an embed never writes the
// host's choice back as the reader's preference.
export function resolveTheme(theme: EmbedTheme, prefersDark: boolean): boolean {
    if (theme === 'dark') return true;
    if (theme === 'light') return false;
    return prefersDark;
}

export function prefersDarkScheme(): boolean {
    return typeof window !== 'undefined' && !!window.matchMedia?.('(prefers-color-scheme: dark)').matches;
}
