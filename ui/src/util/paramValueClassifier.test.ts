import { describe, it, expect } from 'vitest';
import { classifyParamValue } from './paramValueClassifier';

describe('classifyParamValue', () => {
    describe('null-ish', () => {
        it('classifies "null" as null', () => {
            const result = classifyParamValue('null');
            expect(result.kind).toBe('null');
            expect(result.displayValue).toBe('null');
            expect(result.className).toContain('italic');
        });

        it('classifies "undefined" as null', () => {
            const result = classifyParamValue('undefined');
            expect(result.kind).toBe('null');
            expect(result.displayValue).toBe('null');
            expect(result.className).toContain('italic');
        });
    });

    describe('numbers', () => {
        it('classifies "0" as number', () => {
            const result = classifyParamValue('0');
            expect(result.kind).toBe('number');
            expect(result.displayValue).toBe('0');
            expect(result.className).toContain('text-blue-500');
        });

        it('classifies "42" as number', () => {
            expect(classifyParamValue('42').kind).toBe('number');
        });

        it('classifies "-7" as number', () => {
            expect(classifyParamValue('-7').kind).toBe('number');
        });

        it('classifies "3.14" as number', () => {
            expect(classifyParamValue('3.14').kind).toBe('number');
        });

        it('classifies "-0.5" as number', () => {
            expect(classifyParamValue('-0.5').kind).toBe('number');
        });

        it('does not classify "1e10" as number', () => {
            expect(classifyParamValue('1e10').kind).not.toBe('number');
        });
    });

    describe('booleans', () => {
        it('classifies "true" as boolean', () => {
            const result = classifyParamValue('true');
            expect(result.kind).toBe('boolean');
            expect(result.displayValue).toBe('true');
            expect(result.className).toContain('text-orange-500');
        });

        it('classifies "false" as boolean', () => {
            expect(classifyParamValue('false').kind).toBe('boolean');
        });

        it('does not classify "True" as boolean', () => {
            expect(classifyParamValue('True').kind).not.toBe('boolean');
        });

        it('does not classify "FALSE" as boolean', () => {
            expect(classifyParamValue('FALSE').kind).not.toBe('boolean');
        });
    });

    describe('json', () => {
        it('classifies a JSON object as json and pretty-prints it', () => {
            const result = classifyParamValue('{"a":1}');
            expect(result.kind).toBe('json');
            expect(result.displayValue).toBe('{\n  "a": 1\n}');
            expect(result.className).toContain('font-mono');
        });

        it('classifies a JSON array as json', () => {
            const result = classifyParamValue('[1,2,3]');
            expect(result.kind).toBe('json');
            expect(result.displayValue).toBe('[\n  1,\n  2,\n  3\n]');
        });

        it('falls back to string when JSON-shaped input fails to parse', () => {
            expect(classifyParamValue('{not really json').kind).toBe('string');
        });
    });

    describe('strings', () => {
        it('classifies empty string as string', () => {
            const result = classifyParamValue('');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('""');
            expect(result.className).toContain('text-emerald-600');
        });

        it('wraps a plain string in quotes', () => {
            const result = classifyParamValue('hello');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('"hello"');
            expect(result.className).toContain('text-emerald-600');
        });

        it('leaves an already-quoted string unchanged', () => {
            const result = classifyParamValue('"already quoted"');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('"already quoted"');
        });
    });

    describe('with a kind from the report', () => {
        it('classifies "null" with kind null as null', () => {
            const result = classifyParamValue('null', 'null');
            expect(result.kind).toBe('null');
            expect(result.displayValue).toBe('null');
        });

        it('classifies "42" with kind number as number', () => {
            const result = classifyParamValue('42', 'number');
            expect(result.kind).toBe('number');
            expect(result.displayValue).toBe('42');
        });

        it('classifies "true" with kind boolean as boolean', () => {
            const result = classifyParamValue('true', 'boolean');
            expect(result.kind).toBe('boolean');
            expect(result.displayValue).toBe('true');
        });

        it('classifies "null" with kind string as string', () => {
            const result = classifyParamValue('null', 'string');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('"null"');
        });

        it('classifies "42" with kind string as string', () => {
            const result = classifyParamValue('42', 'string');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('"42"');
        });

        it('classifies "true" with kind string as string', () => {
            const result = classifyParamValue('true', 'string');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('"true"');
        });

        it('classifies "" with kind string as string', () => {
            const result = classifyParamValue('', 'string');
            expect(result.kind).toBe('string');
            expect(result.displayValue).toBe('""');
        });

        it('classifies a JSON object with kind string as json', () => {
            const result = classifyParamValue('{"a":1}', 'string');
            expect(result.kind).toBe('json');
            expect(result.displayValue).toBe(JSON.stringify({a: 1}, null, 2));
        });

        it('classifies a JSON object with kind other as json via the heuristic', () => {
            const result = classifyParamValue('{"a":1}', 'other');
            expect(result.kind).toBe('json');
        });

        it('classifies "42" with kind other as number via the heuristic', () => {
            const result = classifyParamValue('42', 'other');
            expect(result.kind).toBe('number');
        });

        it('classifies "42" with no kind as number, unchanged behaviour', () => {
            const result = classifyParamValue('42');
            expect(result.kind).toBe('number');
        });
    });
});
