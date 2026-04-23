export interface EscapeState {
    hasQuery: boolean;
    isCommandOpen: boolean;
    isDialogOpen: boolean;
}

export function shouldClearSearchOnEscape(state: EscapeState): boolean {
    return state.hasQuery && !state.isCommandOpen && !state.isDialogOpen;
}

export function hasOpenDialog(): boolean {
    if (typeof document === 'undefined') return false;
    return document.querySelector('[role="dialog"][data-state="open"]') !== null;
}
