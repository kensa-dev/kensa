import { describe, expect, it } from 'vitest';
import type { SuiteSearchLocation } from '@/lib/suiteSearch';
import { highlightTermFor, nodeIdForLocation, testKeyForLocation } from './suiteSearchNav';

const location = (overrides: Partial<SuiteSearchLocation> = {}): SuiteSearchLocation => ({
    sourceId: 'alpha',
    testId: 'com.example.OrderTest',
    testMethod: 'places order',
    invocation: 0,
    count: 3,
    ...overrides,
});

describe('nodeIdForLocation', () => {
    it('joins sourceId and testId with the site-mode separator', () => {
        expect(nodeIdForLocation(location())).toBe('alpha::com.example.OrderTest');
    });

    it('uses a different sourceId prefix', () => {
        expect(nodeIdForLocation(location({ sourceId: 'beta', testId: 'a.B' }))).toBe('beta::a.B');
    });
});

describe('testKeyForLocation', () => {
    it('is unique per source, test, method and invocation', () => {
        const a = testKeyForLocation(location());
        const b = testKeyForLocation(location({ invocation: 1 }));
        expect(a).not.toBe(b);
    });

    it('is stable for the same coordinates', () => {
        expect(testKeyForLocation(location())).toBe(testKeyForLocation(location()));
    });
});

describe('highlightTermFor', () => {
    it('returns the value for a value-kind active term', () => {
        expect(highlightTermFor({ kind: 'value', query: 'acme-order-12345' })).toBe('acme-order-12345');
    });

    it('returns null for a fixture-kind active term', () => {
        expect(highlightTermFor({ kind: 'fixture', query: 'OrderIdFx' })).toBeNull();
    });

    it('returns null when there is no active term', () => {
        expect(highlightTermFor(null)).toBeNull();
    });
});
