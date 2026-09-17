import { describe, expect, it } from 'vitest';
import { buildHighlightRegex } from './utils';

describe('buildHighlightRegex', () => {
    it('matches each highlight', () => {
        const regex = buildHighlightRegex(['alice', 'bob']);

        expect('hello alice and bob'.split(regex)).toEqual(['hello ', 'alice', ' and ', 'bob', '']);
    });

    it('escapes regex metacharacters in a highlight', () => {
        const regex = buildHighlightRegex(['a.b']);

        expect(regex.test('axb')).toBe(false);
        expect(buildHighlightRegex(['a.b']).test('a.b')).toBe(true);
    });

    it('ignores empty highlights, keeping the rest', () => {
        const regex = buildHighlightRegex(['', 'bob']);

        expect('hello bob'.split(regex)).toEqual(['hello ', 'bob', '']);
    });

    // An empty highlight used to match at every position, splitting each text node into empty
    // fragments and hanging the browser tab.
    it('never matches when every highlight is empty', () => {
        const regex = buildHighlightRegex(['']);

        expect(regex.test('a null value')).toBe(false);
        expect('a null value'.split(regex)).toEqual(['a null value']);
    });

    it('never matches when there are no highlights', () => {
        const regex = buildHighlightRegex([]);

        expect(regex.test('a null value')).toBe(false);
        expect('a null value'.split(regex)).toEqual(['a null value']);
    });
});
