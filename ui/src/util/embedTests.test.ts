import {describe, expect, it} from 'vitest';
import {embedTests} from './embedTests';

const tests = [
    {testMethod: 'first', displayName: 'first one'},
    {testMethod: 'second', displayName: 'second one'},
];

describe('embedTests', () => {
    it('shows the whole class without a method', () => {
        expect(embedTests(tests, null)).toEqual(tests);
    });

    it('shows only the named method', () => {
        expect(embedTests(tests, 'second')).toEqual([tests[1]]);
    });

    it('matches the display name, ignoring case and surrounding space', () => {
        expect(embedTests(tests, ' Second One ')).toEqual([tests[1]]);
    });

    it('shows nothing for a method that is not there', () => {
        expect(embedTests(tests, 'third')).toEqual([]);
    });
});
