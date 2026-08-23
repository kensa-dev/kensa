import {describe, expect, it} from 'vitest';
import {filterIndices} from './filterIndices';
import {parseQuery} from '@/util/queryMeta';
import {Indices} from '@/types/Index';

const tree: Indices = [{
    id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Failed', children: [
        {id: 'overview:a', type: 'overview', displayName: 'Overview', testClass: '', state: 'Passed'},
        {id: 'sysview:a', type: 'system-view', displayName: 'System View', testClass: '', state: 'Passed'},
        {
            id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Failed', epics: ['E-1'], tags: ['slow'], children: [
                {id: 'a::x.FooTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.FooTest', state: 'Passed'},
                {id: 'a::x.FooTest:two', testMethod: 'two', displayName: 'two', testClass: 'x.FooTest', state: 'Failed', epics: ['E-2']},
            ],
        },
        {
            id: 'a::x.BarTest', displayName: 'BarTest', testClass: 'x.BarTest', state: 'Passed', children: [
                {id: 'a::x.BarTest:three', testMethod: 'three', displayName: 'three', testClass: 'x.BarTest', state: 'Passed', epics: ['E-2']},
            ],
        },
    ],
}];

const leaves = (nodes: Indices): string[] => nodes.flatMap(n => n.testClass ? [n.id] : leaves(n.children ?? []));

describe('filterIndices', () => {
    it('returns everything with an empty query', () => {
        const r = filterIndices(tree, parseQuery(''), '');
        expect(leaves(r.filteredIndices)).toEqual(['a::x.FooTest', 'a::x.BarTest']);
    });

    it('keeps overview and system-view nodes under any filter', () => {
        const r = filterIndices(tree, parseQuery('state:failed'), '');
        expect(r.filteredIndices[0].children!.map(c => c.id)).toEqual(['overview:a', 'sysview:a', 'a::x.FooTest']);
    });

    it('filters by class-level epic', () => {
        const r = filterIndices(tree, parseQuery('epic:E-1'), '');
        expect(leaves(r.filteredIndices)).toEqual(['a::x.FooTest']);
        expect(r.filteredIndices[0].children![2].children!.map(c => c.testMethod)).toEqual(['one', 'two']);
    });

    it('filters by method-level epic across classes', () => {
        const r = filterIndices(tree, parseQuery('epic:E-2'), '');
        expect(leaves(r.filteredIndices)).toEqual(['a::x.FooTest', 'a::x.BarTest']);
        expect(r.filteredIndices[0].children![2].children!.map(c => c.testMethod)).toEqual(['two']);
    });

    it('live-filters while typing an epic', () => {
        const r = filterIndices(tree, parseQuery(''), 'epic:E-2');
        expect(leaves(r.filteredIndices)).toEqual(['a::x.FooTest', 'a::x.BarTest']);
    });

    it('matches a Not Executed state with the whitespace-free state:notexecuted term', () => {
        const notExecutedTree: Indices = [{
            id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Not Executed', children: [
                {
                    id: 'a::x.BazTest', displayName: 'BazTest', testClass: 'x.BazTest', state: 'Not Executed', children: [
                        {id: 'a::x.BazTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.BazTest', state: 'Not Executed'},
                        {id: 'a::x.BazTest:two', testMethod: 'two', displayName: 'two', testClass: 'x.BazTest', state: 'Passed'},
                    ],
                },
            ],
        }];

        const r = filterIndices(notExecutedTree, parseQuery('state:notexecuted'), '');

        expect(leaves(r.filteredIndices)).toEqual(['a::x.BazTest']);
        expect(r.filteredIndices[0].children![0].children!.map(c => c.testMethod)).toEqual(['one']);
    });

    it('combines state and tag', () => {
        const r = filterIndices(tree, parseQuery('state:failed tag:slow'), '');
        expect(r.firstMatchingTest?.id).toBe('a::x.FooTest');
        expect(r.firstMatchingMethod).toBe('two');
        expect(r.matchingMethodsMap.get('a::x.FooTest')).toEqual(['two']);
    });
});
