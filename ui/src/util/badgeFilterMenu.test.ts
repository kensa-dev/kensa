import { describe, it, expect } from 'vitest';
import { badgeFilterMenu } from './badgeFilterMenu';

describe('badgeFilterMenu', () => {
    it('offers filter and add for an issue not in the query', () => {
        expect(badgeFilterMenu('tag:smoke', 'issue', 'K-1')).toEqual([
            { key: 'filter', label: 'Filter by this issue', additive: false },
            { key: 'toggle', label: 'Add to filter', additive: true },
        ]);
    });

    it('offers remove when the issue token is already in the query', () => {
        expect(badgeFilterMenu('issue:K-1 issue:K-2', 'issue', 'K-1')).toEqual([
            { key: 'filter', label: 'Filter by this issue', additive: false },
            { key: 'toggle', label: 'Remove from filter', additive: true },
        ]);
    });

    it('names epics in the filter entry', () => {
        expect(badgeFilterMenu('', 'epic', 'E-1')).toEqual([
            { key: 'filter', label: 'Filter by this epic', additive: false },
            { key: 'toggle', label: 'Add to filter', additive: true },
        ]);
    });

    it('does not mistake an issue token for an epic token', () => {
        expect(badgeFilterMenu('issue:K-1', 'epic', 'K-1')[1].label).toBe('Add to filter');
    });
});
