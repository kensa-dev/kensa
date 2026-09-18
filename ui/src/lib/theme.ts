// Light or dark, as this browser holds it. Nothing stored means the OS decides; a stored
// choice decides instead. The head script in index.html stamps from the same key before
// first paint, and embeds never write here (see util/embedTheme.ts).
//
// The pure functions touch no window, so the tests run under node.

export type Theme = 'light' | 'dark';

/** The one key the head script, this module and the hub read. */
export const KEY = 'theme';

/** A stored value is a choice only if it is one of the two themes. */
export const parseStored = (raw: string | null): Theme | null =>
    raw === 'light' || raw === 'dark' ? raw : null;

/** What a page showing this stored choice under this OS setting shows. */
export const effective = (stored: Theme | null, osDark: boolean): Theme =>
    stored ?? (osDark ? 'dark' : 'light');

/** Where the switch goes: two states once a choice has been made, never back to "ask the OS". */
export const next = (showing: Theme): Theme => (showing === 'dark' ? 'light' : 'dark');

/** The switch says where pressing it goes, so nobody has to work out which state they are in. */
export const switchLabel = (showing: Theme): string =>
    showing === 'dark' ? 'Switch to light' : 'Switch to dark';

/** The choice this browser holds, or none. */
export const readTheme = (): Theme | null => {
    try {
        return parseStored(localStorage.getItem(KEY));
    } catch {
        return null;
    }
};

/** Stamps the page and remembers the choice. */
export const applyTheme = (theme: Theme): void => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    try {
        localStorage.setItem(KEY, theme);
    } catch {
        // storage turned off: the page follows the OS next time
    }
};

/** What this page shows now. */
export const showing = (): Theme =>
    effective(readTheme(), typeof window !== 'undefined' && !!window.matchMedia?.('(prefers-color-scheme: dark)').matches);

export interface ThemeInputs {
    embed: boolean;
    embedTheme: 'light' | 'dark' | 'auto' | null;
    themeParam: string | null;
    stored: Theme | null;
    osDark: boolean;
}

/** The order every page agrees on: an explicit ?theme= wins, an embed follows the OS unless told, then the stored choice, then the OS. */
export const initialTheme = ({embed, embedTheme, themeParam, stored, osDark}: ThemeInputs): Theme => {
    if (embed) return embedTheme === 'dark' ? 'dark' : embedTheme === 'light' ? 'light' : osDark ? 'dark' : 'light';
    if (themeParam === 'dark' || themeParam === 'light') return themeParam;
    return effective(stored, osDark);
};
