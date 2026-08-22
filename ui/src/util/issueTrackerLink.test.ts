import { describe, it, expect } from 'vitest';
import { issueHref } from './issueTrackerLink';

describe('issueHref', () => {
    it('returns null when no tracker is configured', () => {
        expect(issueHref(null, 'PROJ-123')).toBeNull();
    });

    it('returns null when the tracker is absent from the config', () => {
        expect(issueHref(undefined, 'PROJ-123')).toBeNull();
    });

    it('returns null for an empty tracker url', () => {
        expect(issueHref('', 'PROJ-123')).toBeNull();
    });

    it('returns null for a whitespace-only tracker url', () => {
        expect(issueHref('   ', 'PROJ-123')).toBeNull();
    });

    it('returns null for the legacy http://empty sentinel', () => {
        expect(issueHref('http://empty', 'PROJ-123')).toBeNull();
    });

    it('appends the issue to a tracker url without a trailing slash', () => {
        expect(issueHref('https://jira.example.com/browse', 'PROJ-123'))
            .toBe('https://jira.example.com/browse/PROJ-123');
    });

    it('does not double the separator when the tracker url ends in a slash', () => {
        expect(issueHref('https://jira.example.com/browse/', 'PROJ-123'))
            .toBe('https://jira.example.com/browse/PROJ-123');
    });
});
