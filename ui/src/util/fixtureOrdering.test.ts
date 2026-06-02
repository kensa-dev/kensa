import { describe, it, expect } from 'vitest';
import { orderedFixtureRows } from './fixtureOrdering';
import type { FixtureSpec, NameAndValues } from '@/types/Test';

describe('orderedFixtureRows', () => {
    it('returns flat rows when fixtureSpecs is undefined', () => {
        const fixtures: NameAndValues = [{ a: '1' }, { b: '2' }];
        expect(orderedFixtureRows(fixtures, undefined)).toEqual([
            { key: 'a', value: '1', depth: 0 },
            { key: 'b', value: '2', depth: 0 },
        ]);
    });

    it('returns an empty list when fixtures is undefined', () => {
        expect(orderedFixtureRows(undefined, undefined)).toEqual([]);
    });

    it('returns flat rows when no spec has parents', () => {
        const fixtures: NameAndValues = [{ a: '1' }, { b: '2' }];
        const specs: FixtureSpec[] = [{ key: 'a', parents: [] }, { key: 'b', parents: [] }];
        expect(orderedFixtureRows(fixtures, specs)).toEqual([
            { key: 'a', value: '1', depth: 0 },
            { key: 'b', value: '2', depth: 0 },
        ]);
    });

    it('orders children under parents with increasing depth', () => {
        const fixtures: NameAndValues = [{ child: 'c' }, { parent: 'p' }];
        const specs: FixtureSpec[] = [
            { key: 'parent', parents: [] },
            { key: 'child', parents: ['parent'] },
        ];
        expect(orderedFixtureRows(fixtures, specs)).toEqual([
            { key: 'parent', value: 'p', depth: 0 },
            { key: 'child', value: 'c', depth: 1 },
        ]);
    });
});
