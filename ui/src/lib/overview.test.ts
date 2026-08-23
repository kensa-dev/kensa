import {describe, expect, it} from 'vitest';
import {buildOverview, packageDepthFor} from './overview';
import {Indices} from '@/types/Index';

const fixture: Indices = [{
    id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Failed', sourceId: 'a', children: [
        {id: 'overview:a', type: 'overview', displayName: 'Overview', testClass: '', state: 'Passed'},
        {
            id: 'a::x.loan.FooTest', displayName: 'FooTest', testClass: 'x.loan.FooTest', state: 'Failed', tags: ['slow'], epics: ['E-1'], issues: ['I-1'], children: [
                {id: 'a::x.loan.FooTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.loan.FooTest', state: 'Passed', tags: ['api']},
                {id: 'a::x.loan.FooTest:two', testMethod: 'two', displayName: 'two', testClass: 'x.loan.FooTest', state: 'Failed', issues: ['I-2']},
            ],
        },
        {
            id: 'a::x.ship.BarTest', displayName: 'BarTest', testClass: 'x.ship.BarTest', state: 'Passed', children: [
                {id: 'a::x.ship.BarTest:three', testMethod: 'three', displayName: 'three', testClass: 'x.ship.BarTest', state: 'Disabled', epics: ['E-1']},
                {id: 'a::x.ship.BarTest:four', testMethod: 'four', displayName: 'four', testClass: 'x.ship.BarTest', state: 'Passed'},
            ],
        },
    ],
}];

describe('buildOverview', () => {
    const m = buildOverview(fixture, 2);

    it('counts results over method leaves', () => {
        expect(m.results).toEqual({passed: 2, failed: 1, disabled: 1, notExecuted: 0, total: 4});
    });

    it('groups by effective tag', () => {
        expect(m.byTag).toEqual([
            {key: 'slow', passed: 1, failed: 1, disabled: 0, notExecuted: 0, total: 2},
            {key: 'api', passed: 1, failed: 0, disabled: 0, notExecuted: 0, total: 1},
        ]);
    });

    it('groups by package at the requested depth', () => {
        expect(m.byPackage).toEqual([
            {key: 'x.loan', passed: 1, failed: 1, disabled: 0, notExecuted: 0, total: 2},
            {key: 'x.ship', passed: 1, failed: 0, disabled: 1, notExecuted: 0, total: 2},
        ]);
    });

    it('groups epics with nested issues and epic-only count', () => {
        expect(m.byEpic).toEqual([{
            epic: 'E-1', passed: 1, failed: 1, disabled: 1, notExecuted: 0, total: 3, epicOnly: 1,
            issues: [
                {issue: 'I-1', passed: 1, failed: 1, disabled: 0, notExecuted: 0, total: 2},
                {issue: 'I-2', passed: 0, failed: 1, disabled: 0, notExecuted: 0, total: 1},
            ],
        }]);
    });

    it('lists failures with effective epics and issues and a concentration hint', () => {
        expect(m.failures).toEqual({
            rows: [{nodeId: 'a::x.loan.FooTest:two', classId: 'a::x.loan.FooTest', testClass: 'x.loan.FooTest', testMethod: 'two', displayName: 'two', epics: ['E-1'], issues: ['I-1', 'I-2']}],
            concentration: null,
        });
    });

    it('returns null slices for an empty tree', () => {
        const e = buildOverview([], 2);
        expect(e.results).toBeNull();
        expect(e.byTag).toBeNull();
        expect(e.byPackage).toBeNull();
        expect(e.byEpic).toBeNull();
        expect(e.failures).toBeNull();
    });

    it('returns null for tag and epic slices when nothing is tagged', () => {
        const bare: Indices = [{id: 'c', displayName: 'C', testClass: 'p.C', state: 'Passed', children: [
            {id: 'c:a', testMethod: 'a', displayName: 'a', testClass: 'p.C', state: 'Passed'},
        ]}];
        const b = buildOverview(bare, 1);
        expect(b.byTag).toBeNull();
        expect(b.byEpic).toBeNull();
        expect(b.failures).toBeNull();
    });

    it('counts not-executed leaves separately from disabled', () => {
        const notExecuted: Indices = [{id: 'c', displayName: 'C', testClass: 'p.C', state: 'Passed', children: [
            {id: 'c:a', testMethod: 'a', displayName: 'a', testClass: 'p.C', state: 'Passed'},
            {id: 'c:b', testMethod: 'b', displayName: 'b', testClass: 'p.C', state: 'Not Executed'},
        ]}];
        const n = buildOverview(notExecuted, 1);
        expect(n.results).toEqual({passed: 1, failed: 0, disabled: 0, notExecuted: 1, total: 2});
    });

    it('sets a concentration hint only when more than one failure exists', () => {
        const threeFailures: Indices = [{
            id: 'e', displayName: 'E', testClass: '', state: 'Failed', children: [
                {
                    id: 'e::x.loan.FooTest', displayName: 'FooTest', testClass: 'x.loan.FooTest', state: 'Failed', children: [
                        {id: 'e::x.loan.FooTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.loan.FooTest', state: 'Failed'},
                        {id: 'e::x.loan.FooTest:two', testMethod: 'two', displayName: 'two', testClass: 'x.loan.FooTest', state: 'Failed'},
                    ],
                },
                {
                    id: 'e::x.ship.BarTest', displayName: 'BarTest', testClass: 'x.ship.BarTest', state: 'Failed', children: [
                        {id: 'e::x.ship.BarTest:three', testMethod: 'three', displayName: 'three', testClass: 'x.ship.BarTest', state: 'Failed'},
                    ],
                },
            ],
        }];
        const f = buildOverview(threeFailures, 2);
        expect(f.failures!.concentration).toEqual({pkg: 'x.loan', count: 2});
    });
});

describe('buildOverview metrics', () => {
    const metrics: Indices = [{
        id: 'c', displayName: 'C', testClass: 'p.C', state: 'Passed', children: [
            {id: 'c:a', testMethod: 'a', displayName: 'a', testClass: 'p.C', state: 'Passed', timing: [[0, 50]], assertions: 3, expandables: 1, interactions: 2, participants: {A: 2, B: 2}},
            {id: 'c:b', testMethod: 'b', displayName: 'b', testClass: 'p.C', state: 'Passed', timing: [[0, 700], [0, 900]], assertions: 1, participants: {A: 1}},
        ],
    }, {
        id: 'd', displayName: 'D', testClass: 'p.D', state: 'Passed', children: [
            {id: 'd:x', testMethod: 'x', displayName: 'x', testClass: 'p.D', state: 'Passed', timing: [[0, 40000]]},
        ],
    }];
    const m = buildOverview(metrics, 1);

    it('buckets durations by summed elapsed per method', () => {
        expect(m.durations).toEqual([
            {label: '<100ms', count: 1},
            {label: '100ms–1s', count: 0},
            {label: '1–5s', count: 1},
            {label: '5–30s', count: 0},
            {label: '>30s', count: 1},
        ]);
    });

    it('lists slowest methods descending, at most ten', () => {
        expect(m.slowest!.map(s => [s.testMethod, s.elapsedMs])).toEqual([['x', 40000], ['b', 1600], ['a', 50]]);
    });

    it('sums participants across methods', () => {
        expect(m.participants).toEqual([{key: 'A', count: 3}, {key: 'B', count: 2}]);
    });

    it('computes density and the weakest class', () => {
        expect(m.density).toEqual({
            assertionsPerTest: 4 / 3,
            expandableShare: 1 / 3,
            parameterised: 1,
            weakest: {classId: 'd', testClass: 'p.D', assertionsPerTest: 0},
        });
    });

    it('returns null metric slices without timing or counts', () => {
        const bare: Indices = [{id: 'c', displayName: 'C', testClass: 'p.C', state: 'Passed', children: [
            {id: 'c:a', testMethod: 'a', displayName: 'a', testClass: 'p.C', state: 'Passed'},
        ]}];
        const b = buildOverview(bare, 1);
        expect(b.durations).toBeNull();
        expect(b.slowest).toBeNull();
        expect(b.participants).toBeNull();
        expect(b.density).toBeNull();
    });
});

describe('packageDepthFor', () => {
    it('trims to one segment below the common root when common packages are hidden', () => {
        expect(packageDepthFor('HideCommonPackages', 'dev.kensa.examples')).toBe(4);
    });

    it('uses the whole package for any other display or a missing root', () => {
        expect(packageDepthFor('Hidden', 'dev.kensa.examples')).toBe(0);
        expect(packageDepthFor('HideCommonPackages', undefined)).toBe(0);
        expect(packageDepthFor('HideCommonPackages', '')).toBe(0);
    });
});
