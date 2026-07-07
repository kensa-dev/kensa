import {describe, expect, it} from 'vitest';
import {substitutePayloadValue} from './fixtureSubstitution';

describe('substitutePayloadValue', () => {
    it('replaces a correlation value with its fixture-name placeholder', () => {
        expect(substitutePayloadValue('{"basketId":"BSK-77"}', [{value: 'BSK-77', name: 'basketId'}]))
            .toBe('{"basketId":"<basketId>"}');
    });

    it('replaces every occurrence of the value', () => {
        expect(substitutePayloadValue('BSK-77 then BSK-77 again', [{value: 'BSK-77', name: 'basketId'}]))
            .toBe('<basketId> then <basketId> again');
    });

    it('applies multiple substitutions', () => {
        const result = substitutePayloadValue('{"basketId":"BSK-77","sku":"RBT-9"}', [
            {value: 'BSK-77', name: 'basketId'},
            {value: 'RBT-9', name: 'sku'},
        ]);
        expect(result).toBe('{"basketId":"<basketId>","sku":"<sku>"}');
    });

    it('treats values literally, not as regex', () => {
        expect(substitutePayloadValue('id is a.b*c', [{value: 'a.b*c', name: 'weird'}]))
            .toBe('id is <weird>');
    });

    it('skips substitutions with an empty value', () => {
        expect(substitutePayloadValue('unchanged', [{value: '', name: 'empty'}])).toBe('unchanged');
    });

    it('returns the payload unchanged when there are no substitutions', () => {
        expect(substitutePayloadValue('unchanged', [])).toBe('unchanged');
    });
});
