export type SuiteSearchAction = 'fixture' | 'none';

export function resolveGesture(e: { metaKey: boolean; ctrlKey: boolean }): SuiteSearchAction {
    return e.metaKey || e.ctrlKey ? 'fixture' : 'none';
}
