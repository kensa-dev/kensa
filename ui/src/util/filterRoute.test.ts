import {describe, expect, it} from 'vitest';
import {filterRouteTarget, parseFilterRoute} from './filterRoute';
import {Indices} from '@/types/Index';

describe('parseFilterRoute', () => {
    it('parses an issue route', () => {
        expect(parseFilterRoute('/issue/KEN-7')).toEqual({query: 'issue:KEN-7'});
    });

    it('parses an epic route', () => {
        expect(parseFilterRoute('/epic/EP-1')).toEqual({query: 'epic:EP-1'});
    });

    it('decodes a percent-encoded key', () => {
        expect(parseFilterRoute('/issue/KEN%2D7')).toEqual({query: 'issue:KEN-7'});
    });

    it('returns null for an issue route with no key', () => {
        expect(parseFilterRoute('/issue/')).toBeNull();
    });

    it('falls back to the raw key when it is not valid percent-encoding', () => {
        expect(parseFilterRoute('/issue/50%')).toEqual({query: 'issue:50%'});
    });

    it('returns null for a test route', () => {
        expect(parseFilterRoute('/test/x')).toBeNull();
    });

    it('returns null for the overview route', () => {
        expect(parseFilterRoute('/overview')).toBeNull();
    });
});

describe('filterRouteTarget', () => {
    it('resolves a class-level issue to the class and its first method', () => {
        const indices: Indices = [{
            id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
                {
                    id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', issues: ['KEN-7'], children: [
                        {id: 'a::x.FooTest:first', testMethod: 'first', displayName: 'first', testClass: 'x.FooTest', state: 'Passed'},
                        {id: 'a::x.FooTest:second', testMethod: 'second', displayName: 'second', testClass: 'x.FooTest', state: 'Passed'},
                    ],
                },
            ],
        }];
        expect(filterRouteTarget(indices, {query: 'issue:KEN-7'})).toEqual({testId: 'a::x.FooTest', method: 'first'});
    });

    it('resolves a method-level issue to the second of three methods', () => {
        const indices: Indices = [{
            id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
                {
                    id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', children: [
                        {id: 'a::x.FooTest:first', testMethod: 'first', displayName: 'first', testClass: 'x.FooTest', state: 'Passed'},
                        {id: 'a::x.FooTest:second', testMethod: 'second', displayName: 'second', testClass: 'x.FooTest', state: 'Passed', issues: ['KEN-7']},
                        {id: 'a::x.FooTest:third', testMethod: 'third', displayName: 'third', testClass: 'x.FooTest', state: 'Passed'},
                    ],
                },
            ],
        }];
        expect(filterRouteTarget(indices, {query: 'issue:KEN-7'})).toEqual({testId: 'a::x.FooTest', method: 'second'});
    });

    it('resolves a method-level issue in the second of two sources', () => {
        const indices: Indices = [
            {
                id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
                    {
                        id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', children: [
                            {id: 'a::x.FooTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.FooTest', state: 'Passed'},
                        ],
                    },
                ],
            },
            {
                id: 'src:b', type: 'project', displayName: 'B', testClass: '', state: 'Passed', children: [
                    {
                        id: 'b::y.BarTest', displayName: 'BarTest', testClass: 'y.BarTest', state: 'Passed', children: [
                            {id: 'b::y.BarTest:two', testMethod: 'two', displayName: 'two', testClass: 'y.BarTest', state: 'Passed', issues: ['KEN-9']},
                        ],
                    },
                ],
            },
        ];
        expect(filterRouteTarget(indices, {query: 'issue:KEN-9'})).toEqual({testId: 'b::y.BarTest', method: 'two'});
    });

    it('returns null when no node carries the key', () => {
        const indices: Indices = [{
            id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
                {
                    id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', issues: ['KEN-7'], children: [
                        {id: 'a::x.FooTest:first', testMethod: 'first', displayName: 'first', testClass: 'x.FooTest', state: 'Passed'},
                        {id: 'a::x.FooTest:second', testMethod: 'second', displayName: 'second', testClass: 'x.FooTest', state: 'Passed'},
                    ],
                },
            ],
        }];
        expect(filterRouteTarget(indices, {query: 'issue:KEN-404'})).toBeNull();
    });

    it('resolves an epic on a method', () => {
        const indices: Indices = [{
            id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
                {
                    id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', children: [
                        {id: 'a::x.FooTest:one', testMethod: 'one', displayName: 'one', testClass: 'x.FooTest', state: 'Passed', epics: ['EP-1']},
                    ],
                },
            ],
        }];
        expect(filterRouteTarget(indices, {query: 'epic:EP-1'})).toEqual({testId: 'a::x.FooTest', method: 'one'});
    });
});
