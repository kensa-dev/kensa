import { describe, it, expect } from 'vitest';
import { tagMatch } from './tagMatch';

describe('tagMatch', () => {
    it('returns true when the selection is empty', () => {
        expect(tagMatch(['slow'], new Set())).toBe(true);
    });

    it('returns true when the selection is empty and test tags are undefined', () => {
        expect(tagMatch(undefined, new Set())).toBe(true);
    });

    it('returns true when any selected tag is present on the test', () => {
        expect(tagMatch(['slow', 'flaky'], new Set(['flaky']))).toBe(true);
    });

    it('returns true when the tag sets overlap on multiple values', () => {
        expect(tagMatch(['a', 'b', 'c'], new Set(['c', 'd']))).toBe(true);
    });

    it('returns false when the sets are disjoint', () => {
        expect(tagMatch(['slow'], new Set(['fast']))).toBe(false);
    });

    it('returns false when test tags are undefined and a filter is active', () => {
        expect(tagMatch(undefined, new Set(['slow']))).toBe(false);
    });

    it('returns false when test tags are empty and a filter is active', () => {
        expect(tagMatch([], new Set(['slow']))).toBe(false);
    });
});
