import {describe, expect, it} from 'vitest';
import {embedParams, embedTarget, parseEmbedRoute} from './embedRoute';
import {Indices} from '@/types/Index';

describe('parseEmbedRoute', () => {
    it('parses an embed route to its test id', () => {
        expect(parseEmbedRoute('/embed/test::a.B')).toEqual({testId: 'test::a.B'});
    });

    it('decodes a percent-encoded test id', () => {
        expect(parseEmbedRoute('/embed/test%3A%3Aa.B')).toEqual({testId: 'test::a.B'});
    });

    it('falls back to the raw id when it is not valid percent-encoding', () => {
        expect(parseEmbedRoute('/embed/50%')).toEqual({testId: '50%'});
    });

    it('returns null for an embed route with no id', () => {
        expect(parseEmbedRoute('/embed/')).toBeNull();
    });

    it('returns null for a test route', () => {
        expect(parseEmbedRoute('/test/x')).toBeNull();
    });
});

describe('embedParams', () => {
    it('reads every param', () => {
        expect(embedParams(new URLSearchParams('method=m&invocation=2&theme=dark&notes=1')))
            .toEqual({method: 'm', invocation: 2, theme: 'dark', notes: true});
    });

    it('defaults when nothing is given', () => {
        expect(embedParams(new URLSearchParams('')))
            .toEqual({method: null, invocation: -1, theme: 'auto', notes: false});
    });

    it('accepts light and auto themes', () => {
        expect(embedParams(new URLSearchParams('theme=light')).theme).toBe('light');
        expect(embedParams(new URLSearchParams('theme=auto')).theme).toBe('auto');
    });

    it('treats an unknown theme as auto', () => {
        expect(embedParams(new URLSearchParams('theme=sepia')).theme).toBe('auto');
    });

    it('treats a bad invocation as unset', () => {
        expect(embedParams(new URLSearchParams('invocation=x')).invocation).toBe(-1);
        expect(embedParams(new URLSearchParams('invocation=-3')).invocation).toBe(-1);
    });

    it('treats notes as set only when 1 or true', () => {
        expect(embedParams(new URLSearchParams('notes=true')).notes).toBe(true);
        expect(embedParams(new URLSearchParams('notes=0')).notes).toBe(false);
    });
});

describe('embedTarget', () => {
    const indices: Indices = [{
        id: 'src:a', type: 'project', displayName: 'A', testClass: '', state: 'Passed', children: [
            {
                id: 'a::x.FooTest', displayName: 'FooTest', testClass: 'x.FooTest', state: 'Passed', children: [
                    {id: 'a::x.FooTest:first', testMethod: 'first', displayName: 'first one', testClass: 'x.FooTest', state: 'Passed'},
                    {id: 'a::x.FooTest:second', testMethod: 'second', displayName: 'second one', testClass: 'x.FooTest', state: 'Passed'},
                ],
            },
        ],
    }];

    it('finds a class', () => {
        expect(embedTarget(indices, 'a::x.FooTest', null)).toEqual({found: true});
    });

    it('finds a method by name or display name, ignoring case and surrounding space', () => {
        expect(embedTarget(indices, 'a::x.FooTest', 'second')).toEqual({found: true});
        expect(embedTarget(indices, 'a::x.FooTest', ' Second One ')).toEqual({found: true});
    });

    it('reports a missing class', () => {
        expect(embedTarget(indices, 'a::x.BarTest', null)).toEqual({found: false, missing: 'test'});
    });

    it('reports a missing method on a found class', () => {
        expect(embedTarget(indices, 'a::x.FooTest', 'third')).toEqual({found: false, missing: 'method'});
    });
});
