import { describe, it, expect } from 'vitest';
import { matchesAnyIssue } from './issueMatch';

describe('matchesAnyIssue', () => {
    it('matches when the node has the exact issue id', () => {
        expect(matchesAnyIssue(['TEAM-1'], ['TEAM-1'])).toBe(true);
    });

    it('does not match TEAM-10 when the filter is TEAM-1', () => {
        expect(matchesAnyIssue(['TEAM-10'], ['TEAM-1'])).toBe(false);
    });

    it('does not match TEAM-123 when the filter is TEAM-1', () => {
        expect(matchesAnyIssue(['TEAM-123'], ['TEAM-1'])).toBe(false);
    });

    it('is case-insensitive', () => {
        expect(matchesAnyIssue(['team-1'], ['TEAM-1'])).toBe(true);
        expect(matchesAnyIssue(['TEAM-1'], ['team-1'])).toBe(true);
    });

    it('matches if any required issue is present', () => {
        expect(matchesAnyIssue(['TEAM-2', 'TEAM-7'], ['TEAM-1', 'TEAM-7'])).toBe(true);
    });

    it('returns false when no issues match', () => {
        expect(matchesAnyIssue(['TEAM-2'], ['TEAM-1'])).toBe(false);
    });

    it('returns false when the node has no issues', () => {
        expect(matchesAnyIssue([], ['TEAM-1'])).toBe(false);
    });
});
