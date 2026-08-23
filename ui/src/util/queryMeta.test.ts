import {describe, expect, it} from 'vitest';
import {appendTerm, isFilterTerm, parseQuery} from './queryMeta';

describe('parseQuery', () => {
    it('splits typed terms from free text', () => {
        expect(parseQuery('issue:A-1 epic:E-9 state:Failed tag:slow pkg:dev.x hello world')).toEqual({
            text: 'hello world', issues: ['A-1'], epics: ['E-9'], states: ['failed'], tags: ['slow'], packages: ['dev.x'],
        });
    });

    it('treats bare prefixes as text', () => {
        expect(parseQuery('epic: issue:').text).toBe('epic: issue:');
    });
});

describe('isFilterTerm', () => {
    it('recognises every prefix', () => {
        for (const t of ['issue:x', 'epic:x', 'state:x', 'tag:x', 'pkg:x']) expect(isFilterTerm(t)).toBe(true);
        expect(isFilterTerm('issue:')).toBe(false);
        expect(isFilterTerm('hello')).toBe(false);
    });
});

describe('appendTerm', () => {
    it('appends once', () => {
        expect(appendTerm('', 'epic:E-1')).toBe('epic:E-1');
        expect(appendTerm('tag:a', 'epic:E-1')).toBe('tag:a epic:E-1');
        expect(appendTerm('tag:a epic:E-1', 'epic:E-1')).toBe('tag:a epic:E-1');
    });
});
