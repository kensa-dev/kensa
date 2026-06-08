import { describe, expect, it } from 'vitest';
import type { MergedIndex } from '@/lib/suiteSearch';
import type { NameAndValues } from '@/types/Test';
import { fixtureNamesForValue } from './fixtureNameResolver';

const index: MergedIndex = {
    terms: [
        { value: 'acme-order-12345', names: ['OrderIdFx', 'RefFx'], locations: [] },
        { value: 'other', names: ['OtherFx'], locations: [] },
    ],
};

describe('fixtureNamesForValue', () => {
    it('returns names from the merged index for a known value', () => {
        expect(fixtureNamesForValue('acme-order-12345', index, [])).toEqual(['OrderIdFx', 'RefFx']);
    });

    it('falls back to local fixtures when the index has no entry', () => {
        const fixtures: NameAndValues = [{ LocalFx: 'unindexed' }];
        expect(fixtureNamesForValue('unindexed', index, fixtures)).toEqual(['LocalFx']);
    });

    it('prefers the merged index over local fixtures', () => {
        const fixtures: NameAndValues = [{ LocalFx: 'acme-order-12345' }];
        expect(fixtureNamesForValue('acme-order-12345', index, fixtures)).toEqual(['OrderIdFx', 'RefFx']);
    });

    it('returns an empty array when nothing matches', () => {
        expect(fixtureNamesForValue('nope', index, [])).toEqual([]);
    });

    it('deduplicates local fixture names', () => {
        const fixtures: NameAndValues = [{ DupFx: 'v' }, { DupFx: 'v' }];
        expect(fixtureNamesForValue('v', index, fixtures)).toEqual(['DupFx']);
    });
});
