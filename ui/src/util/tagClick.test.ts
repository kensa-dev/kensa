import { describe, it, expect } from 'vitest';
import { nextQueryAfterTagClick, selectedTagsFromQuery } from './tagClick';

describe('nextQueryAfterTagClick', () => {
    describe('replace (no modifier)', () => {
        it('adds the clicked tag to an empty query', () => {
            expect(nextQueryAfterTagClick('', 'smoke', false)).toBe('tag:smoke');
        });

        it('replaces all existing tag tokens with the clicked tag', () => {
            expect(nextQueryAfterTagClick('tag:slow tag:flaky', 'smoke', false)).toBe('tag:smoke');
        });

        it('preserves non-tag tokens when replacing tags', () => {
            expect(nextQueryAfterTagClick('state:passed tag:slow issue:K-1', 'smoke', false))
                .toBe('state:passed issue:K-1 tag:smoke');
        });

        it('replaces with the same tag when only that tag is present', () => {
            expect(nextQueryAfterTagClick('tag:smoke', 'smoke', false)).toBe('tag:smoke');
        });

        it('collapses whitespace and trims', () => {
            expect(nextQueryAfterTagClick('  tag:slow   foo  ', 'smoke', false)).toBe('foo tag:smoke');
        });
    });

    describe('additive (modifier held)', () => {
        it('adds the clicked tag to an empty query', () => {
            expect(nextQueryAfterTagClick('', 'smoke', true)).toBe('tag:smoke');
        });

        it('appends the clicked tag to an existing tag selection', () => {
            expect(nextQueryAfterTagClick('tag:slow', 'smoke', true)).toBe('tag:slow tag:smoke');
        });

        it('removes the clicked tag when already selected', () => {
            expect(nextQueryAfterTagClick('tag:smoke', 'smoke', true)).toBe('');
        });

        it('removes only the clicked tag and keeps other tags', () => {
            expect(nextQueryAfterTagClick('tag:smoke tag:slow', 'smoke', true)).toBe('tag:slow');
        });

        it('preserves non-tag tokens when toggling a tag off', () => {
            expect(nextQueryAfterTagClick('state:passed tag:smoke issue:K-1', 'smoke', true))
                .toBe('state:passed issue:K-1');
        });

        it('preserves non-tag tokens when appending a tag', () => {
            expect(nextQueryAfterTagClick('state:passed', 'smoke', true)).toBe('state:passed tag:smoke');
        });
    });
});

describe('selectedTagsFromQuery', () => {
    it('returns an empty set for an empty query', () => {
        expect(selectedTagsFromQuery('')).toEqual(new Set());
    });

    it('returns an empty set when no tag tokens are present', () => {
        expect(selectedTagsFromQuery('state:passed foo issue:K-1')).toEqual(new Set());
    });

    it('extracts a single tag', () => {
        expect(selectedTagsFromQuery('tag:smoke')).toEqual(new Set(['smoke']));
    });

    it('extracts multiple tags', () => {
        expect(selectedTagsFromQuery('tag:smoke tag:slow')).toEqual(new Set(['smoke', 'slow']));
    });

    it('ignores tag: tokens with no value', () => {
        expect(selectedTagsFromQuery('tag: tag:smoke')).toEqual(new Set(['smoke']));
    });

    it('preserves tag tokens alongside other token kinds', () => {
        expect(selectedTagsFromQuery('state:passed tag:smoke issue:K-1 foo')).toEqual(new Set(['smoke']));
    });

    it('handles extra whitespace', () => {
        expect(selectedTagsFromQuery('  tag:smoke   tag:slow  ')).toEqual(new Set(['smoke', 'slow']));
    });
});
