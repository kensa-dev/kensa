import {describe, expect, it} from 'vitest';
import {collectIntervals, computeTiming} from './overviewTiming';
import {Indices} from '@/types/Index';

describe('computeTiming', () => {
    it('returns null without intervals', () => {
        expect(computeTiming([])).toBeNull();
    });

    it('single test has speedup 1 and peak 1', () => {
        const t = computeTiming([[1000, 500]])!;
        expect(t).toMatchObject({wallClockMs: 500, totalElapsedMs: 500, speedup: 1, savedMs: 0, peak: 1});
        expect(t.steps).toEqual([{t: 0, running: 1}, {t: 500, running: 0}]);
    });

    it('overlapping tests raise peak and speedup', () => {
        const t = computeTiming([[0, 1000], [200, 1000], [400, 1000]])!;
        expect(t.wallClockMs).toBe(1400);
        expect(t.totalElapsedMs).toBe(3000);
        expect(t.peak).toBe(3);
        expect(t.savedMs).toBe(1600);
        expect(t.speedup).toBeCloseTo(3000 / 1400);
    });

    it('back-to-back tests at the same timestamp do not overlap', () => {
        const t = computeTiming([[0, 100], [100, 100]])!;
        expect(t.peak).toBe(1);
        expect(t.steps).toEqual([{t: 0, running: 1}, {t: 100, running: 0}, {t: 100, running: 1}, {t: 200, running: 0}]);
    });

    it('buckets long step lists to at most 200 points keeping the max per bucket', () => {
        const intervals: [number, number][] = Array.from({length: 500}, (_, i) => [i * 10, 25]);
        const t = computeTiming(intervals)!;
        expect(t.steps.length).toBeLessThanOrEqual(200);
        expect(Math.max(...t.steps.map(s => s.running))).toBe(t.peak);
    });
});

describe('collectIntervals', () => {
    it('walks nested children and flattens timing pairs', () => {
        const nodes: Indices = [{
            id: 'p', type: 'package', displayName: 'p', testClass: '', state: 'Passed', children: [
                {id: 'c', displayName: 'C', testClass: 'C', state: 'Passed', children: [
                    {id: 'c:a', testMethod: 'a', displayName: 'a', testClass: 'C', state: 'Passed', timing: [[1, 2], [3, 4]]},
                    {id: 'c:b', testMethod: 'b', displayName: 'b', testClass: 'C', state: 'Passed'},
                ]},
            ],
        }];
        expect(collectIntervals(nodes)).toEqual([[1, 2], [3, 4]]);
    });
});
