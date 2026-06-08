import { describe, it, expect } from 'vitest';
import { mergeIndexes, findByValue, findByFixtureName, autocomplete, groupByProject, summary } from './suiteSearch';
import type { SuiteSearchLocation } from './suiteSearch';
import type { RawSearchIndex } from '../types/SearchIndex';

const indexA: RawSearchIndex = {
    schemaVersion: 1,
    terms: [
        {
            value: 'acme-order-12345',
            names: ['OrderIdFx'],
            locations: [{ testId: 'com.example.OrderTest', testMethod: 'places order', invocation: 0, count: 4 }],
        },
    ],
};

const indexB: RawSearchIndex = {
    schemaVersion: 1,
    terms: [
        {
            value: 'acme-order-12345',
            names: ['AltOrderIdFx', 'OrderIdFx'],
            locations: [{ testId: 'com.other.ShipTest', testMethod: 'ships order', invocation: 1, count: 2 }],
        },
    ],
};

describe('mergeIndexes', () => {
    it('unions a shared value across sources with sourceId-tagged locations and merged sorted names', () => {
        const merged = mergeIndexes([
            { sourceId: 'projA', index: indexA },
            { sourceId: 'projB', index: indexB },
        ]);

        expect(merged.terms).toHaveLength(1);
        const term = merged.terms[0];
        expect(term.value).toBe('acme-order-12345');
        expect(term.names).toEqual(['AltOrderIdFx', 'OrderIdFx']);
        expect(term.locations).toEqual([
            { testId: 'com.example.OrderTest', testMethod: 'places order', invocation: 0, count: 4, sourceId: 'projA' },
            { testId: 'com.other.ShipTest', testMethod: 'ships order', invocation: 1, count: 2, sourceId: 'projB' },
        ]);
    });

    it('tolerates an empty source list', () => {
        expect(mergeIndexes([]).terms).toEqual([]);
    });

    it('tolerates a source with no terms', () => {
        const merged = mergeIndexes([{ sourceId: 'empty', index: { schemaVersion: 1, terms: [] } }]);
        expect(merged.terms).toEqual([]);
    });
});

describe('findByValue', () => {
    it('returns cross-source locations for an exact value match', () => {
        const merged = mergeIndexes([
            { sourceId: 'projA', index: indexA },
            { sourceId: 'projB', index: indexB },
        ]);

        expect(findByValue(merged, 'acme-order-12345')).toEqual([
            { testId: 'com.example.OrderTest', testMethod: 'places order', invocation: 0, count: 4, sourceId: 'projA' },
            { testId: 'com.other.ShipTest', testMethod: 'ships order', invocation: 1, count: 2, sourceId: 'projB' },
        ]);
    });

    it('returns an empty array when the value is absent', () => {
        const merged = mergeIndexes([{ sourceId: 'projA', index: indexA }]);
        expect(findByValue(merged, 'nope')).toEqual([]);
    });
});

describe('findByFixtureName', () => {
    const sharedName: RawSearchIndex = {
        schemaVersion: 1,
        terms: [
            {
                value: 'order-1',
                names: ['OrderIdFx'],
                locations: [{ testId: 'a.T1', testMethod: 'm1', invocation: 0, count: 1 }],
            },
            {
                value: 'unrelated',
                names: ['CustomerFx'],
                locations: [{ testId: 'a.T1', testMethod: 'm1', invocation: 0, count: 1 }],
            },
        ],
    };
    const otherSource: RawSearchIndex = {
        schemaVersion: 1,
        terms: [
            {
                value: 'order-2',
                names: ['OrderIdFx'],
                locations: [{ testId: 'b.T2', testMethod: 'm2', invocation: 0, count: 3 }],
            },
        ],
    };

    it('returns every value for a fixture name across sources with their locations', () => {
        const merged = mergeIndexes([
            { sourceId: 'projA', index: sharedName },
            { sourceId: 'projB', index: otherSource },
        ]);

        expect(findByFixtureName(merged, 'OrderIdFx')).toEqual([
            {
                value: 'order-1',
                locations: [{ testId: 'a.T1', testMethod: 'm1', invocation: 0, count: 1, sourceId: 'projA' }],
            },
            {
                value: 'order-2',
                locations: [{ testId: 'b.T2', testMethod: 'm2', invocation: 0, count: 3, sourceId: 'projB' }],
            },
        ]);
    });

    it('returns an empty array for an unknown fixture name', () => {
        const merged = mergeIndexes([{ sourceId: 'projA', index: sharedName }]);
        expect(findByFixtureName(merged, 'NopeFx')).toEqual([]);
    });
});

describe('autocomplete', () => {
    const acIndex: RawSearchIndex = {
        schemaVersion: 1,
        terms: [
            { value: 'order-alpha', names: ['OrderIdFx'], locations: [] },
            { value: 'beta-order', names: ['BetaFx'], locations: [] },
            { value: 'gamma', names: ['HelperFx'], locations: [] },
            { value: 'has-order-mid', names: ['MidFx'], locations: [] },
            { value: 'zeta', names: ['ZetaFx'], locations: [] },
        ],
    };

    it('returns an empty array for an empty query', () => {
        const merged = mergeIndexes([{ sourceId: 's', index: acIndex }]);
        expect(autocomplete(merged, '', 10)).toEqual([]);
    });

    it('ranks prefix matches before other substring matches then alphabetical', () => {
        const merged = mergeIndexes([{ sourceId: 's', index: acIndex }]);
        expect(autocomplete(merged, 'order', 10)).toEqual([
            { value: 'order-alpha', names: ['OrderIdFx'] },
            { value: 'beta-order', names: ['BetaFx'] },
            { value: 'has-order-mid', names: ['MidFx'] },
        ]);
    });

    it('matches names case-insensitively', () => {
        const merged = mergeIndexes([{ sourceId: 's', index: acIndex }]);
        expect(autocomplete(merged, 'ZETAFX', 10)).toEqual([{ value: 'zeta', names: ['ZetaFx'] }]);
    });

    it('respects the limit', () => {
        const merged = mergeIndexes([{ sourceId: 's', index: acIndex }]);
        expect(autocomplete(merged, 'order', 1)).toEqual([{ value: 'order-alpha', names: ['OrderIdFx'] }]);
    });
});

describe('groupByProject', () => {
    it('groups locations by sourceId in first-seen order', () => {
        const locations: SuiteSearchLocation[] = [
            { testId: 'a.T1', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projB' },
            { testId: 'a.T2', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projA' },
            { testId: 'a.T3', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projB' },
        ];

        expect(groupByProject(locations)).toEqual([
            {
                sourceId: 'projB',
                locations: [
                    { testId: 'a.T1', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projB' },
                    { testId: 'a.T3', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projB' },
                ],
            },
            {
                sourceId: 'projA',
                locations: [{ testId: 'a.T2', testMethod: 'm', invocation: 0, count: 1, sourceId: 'projA' }],
            },
        ]);
    });

    it('returns an empty array for no locations', () => {
        expect(groupByProject([])).toEqual([]);
    });
});

describe('summary', () => {
    it('counts distinct tests and projects', () => {
        const merged = mergeIndexes([
            { sourceId: 'projA', index: indexA },
            { sourceId: 'projB', index: indexB },
        ]);

        expect(summary(findByValue(merged, 'acme-order-12345'))).toEqual({ tests: 2, projects: 2 });
    });

    it('counts the same test once across distinct occurrence rows', () => {
        const locations: SuiteSearchLocation[] = [
            { testId: 'a.T1', testMethod: 'm', invocation: 0, count: 4, sourceId: 'projA' },
            { testId: 'a.T1', testMethod: 'm', invocation: 0, count: 9, sourceId: 'projA' },
            { testId: 'a.T1', testMethod: 'm', invocation: 1, count: 1, sourceId: 'projA' },
        ];

        expect(summary(locations)).toEqual({ tests: 2, projects: 1 });
    });
});
