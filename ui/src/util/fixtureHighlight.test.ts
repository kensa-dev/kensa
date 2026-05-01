import { describe, it, expect } from 'vitest';
import { isRelatedFixture } from './fixtureHighlight';
import type { FixtureSpec, NameAndValues } from '@/types/Test';

describe('isRelatedFixture', () => {
    const fixtures: NameAndValues = [
        { parent: 'parentValue' },
        { child: 'childValue' },
        { unrelated: 'unrelatedValue' },
    ];
    const specs: FixtureSpec[] = [
        { key: 'parent', parents: [] },
        { key: 'child', parents: ['parent'] },
        { key: 'unrelated', parents: [] },
    ];

    it('returns false when selected and this are the same value', () => {
        expect(isRelatedFixture('parentValue', 'parentValue', fixtures, specs)).toBe(false);
    });

    it('returns true when this is a child of selected', () => {
        expect(isRelatedFixture('parentValue', 'childValue', fixtures, specs)).toBe(true);
    });

    it('returns true when this is a parent of selected', () => {
        expect(isRelatedFixture('childValue', 'parentValue', fixtures, specs)).toBe(true);
    });

    it('returns false for unrelated fixtures', () => {
        expect(isRelatedFixture('parentValue', 'unrelatedValue', fixtures, specs)).toBe(false);
    });

    it('returns false when selected value not found in fixtures', () => {
        expect(isRelatedFixture('unknown', 'childValue', fixtures, specs)).toBe(false);
    });

    it('returns false when this value not found in fixtures', () => {
        expect(isRelatedFixture('parentValue', 'unknown', fixtures, specs)).toBe(false);
    });
});
