import { describe, it, expect } from 'vitest';
import { shouldClearSearchOnEscape } from './escapeGuard';

describe('shouldClearSearchOnEscape', () => {
    it('returns true when there is a query and nothing else is open', () => {
        expect(shouldClearSearchOnEscape({ hasQuery: true, isCommandOpen: false, isDialogOpen: false })).toBe(true);
    });

    it('returns false when the query is empty', () => {
        expect(shouldClearSearchOnEscape({ hasQuery: false, isCommandOpen: false, isDialogOpen: false })).toBe(false);
    });

    it('returns false when the command palette is open', () => {
        expect(shouldClearSearchOnEscape({ hasQuery: true, isCommandOpen: true, isDialogOpen: false })).toBe(false);
    });

    it('returns false when any dialog is open', () => {
        expect(shouldClearSearchOnEscape({ hasQuery: true, isCommandOpen: false, isDialogOpen: true })).toBe(false);
    });
});
