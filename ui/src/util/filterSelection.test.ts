import {describe, expect, it} from 'vitest';
import {resolveFilterSelection} from './filterSelection';
import {Index} from '@/types/Index';

const foo: Index = {id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed'};

const matches = new Map<string, string[]>([
    ['a::x.FooTest', ['one']],
    ['a::x.BarTest', ['three', 'four']],
]);

describe('resolveFilterSelection', () => {
    it('keeps the path test when it matches the filter', () => {
        expect(resolveFilterSelection('a::x.BarTest', foo, 'one', matches)).toEqual({testId: 'a::x.BarTest', method: 'three'});
    });

    it('falls back to the first match when the path test does not match the filter', () => {
        expect(resolveFilterSelection('a::x.BazTest', foo, 'one', matches)).toEqual({testId: 'a::x.FooTest', method: 'one'});
    });

    it('falls back to the first match when there is no path test', () => {
        expect(resolveFilterSelection(null, foo, 'one', matches)).toEqual({testId: 'a::x.FooTest', method: 'one'});
    });

    it('selects nothing when nothing matches', () => {
        expect(resolveFilterSelection('a::x.FooTest', null, null, new Map())).toEqual({testId: null, method: ''});
    });

    it('keeps the path test with no method when it matches at class level only', () => {
        const classOnly = new Map<string, string[]>([['a::x.BarTest', []]]);
        expect(resolveFilterSelection('a::x.BarTest', foo, null, classOnly)).toEqual({testId: 'a::x.BarTest', method: ''});
    });
});
