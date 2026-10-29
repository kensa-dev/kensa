import {describe, expect, it} from 'vitest';
import {inheritedKeywords, keywordClass} from './keywords';
import {Token} from '@/types/Test';

const kw = (value: string): Token => ({types: ['tk-kw'], value});
const wd = (value: string): Token => ({types: ['tk-wd'], value});
const nl: Token = {types: ['tk-nl'], value: ''};

describe('inheritedKeywords', () => {
    it('a nested And takes the colour of the Then in the same sentence', () => {
        const sentence = [kw('Then'), wd('a'), nl, kw('And'), wd('b'), nl, kw('And'), wd('c')];

        const inherited = inheritedKeywords(sentence, undefined);

        expect(keywordClass('And', inherited[3])).toContain('text-emerald-600');
        expect(keywordClass('And', inherited[6])).toContain('text-emerald-600');
    });

    it('a leading And keeps the keyword inherited from the previous sentence', () => {
        const sentence = [kw('And'), wd('a'), nl, kw('And'), wd('b')];

        const inherited = inheritedKeywords(sentence, 'when');

        expect(keywordClass('And', inherited[0])).toContain('text-amber-600');
        expect(keywordClass('And', inherited[3])).toContain('text-amber-600');
    });

    it('a later major keyword replaces the inherited one', () => {
        const sentence = [kw('Given'), wd('a'), nl, kw('When'), wd('b'), nl, kw('And'), wd('c')];

        const inherited = inheritedKeywords(sentence, undefined);

        expect(keywordClass('And', inherited[6])).toContain('text-amber-600');
    });

    it('non-keyword tokens carry nothing', () => {
        expect(inheritedKeywords([kw('Then'), wd('a')], undefined)[1]).toBeUndefined();
    });
});
