import { describe, it, expect } from 'vitest';
import { summarizeInvocation } from './invocationSummary';

describe('summarizeInvocation', () => {
    describe('displayName wins', () => {
        it('returns displayName when set', () => {
            const result = summarizeInvocation({ displayName: 'override', parameters: [{ a: '1' }] });
            expect(result).toEqual({ kind: 'displayName', text: 'override' });
        });

        it('treats empty displayName as missing', () => {
            const result = summarizeInvocation({ displayName: '', parameters: [] });
            expect(result).toEqual({ kind: 'empty', text: 'Invocation' });
        });

        it('strips a redundant leading [N] index prefix', () => {
            const result = summarizeInvocation({
                displayName: '[1] scenario=happy path, region=EMEA',
                parameters: [{ region: 'EMEA' }],
            });
            expect(result).toEqual({ kind: 'displayName', text: 'scenario=happy path, region=EMEA' });
        });

        it('strips multi-digit index prefixes', () => {
            const result = summarizeInvocation({
                displayName: '[12] name=foo',
                parameters: [{ name: 'foo' }],
            });
            expect(result).toEqual({ kind: 'displayName', text: 'name=foo' });
        });

        it('only strips a leading index, leaving later bracketed text intact', () => {
            const result = summarizeInvocation({
                displayName: '[1] [tagged] value',
                parameters: [{ a: '1' }],
            });
            expect(result).toEqual({ kind: 'displayName', text: '[tagged] value' });
        });

        it('preserves a bare bracketed value with no trailing space (e.g. a Kotest list toString)', () => {
            const result = summarizeInvocation({
                displayName: '[123]',
                parameters: [],
            });
            expect(result).toEqual({ kind: 'displayName', text: '[123]' });
        });

        it('falls through to params when the index prefix leaves nothing', () => {
            const result = summarizeInvocation({
                displayName: '[1] ',
                parameters: [{ region: 'EMEA' }],
            });
            expect(result).toEqual({ kind: 'inline', text: 'region: EMEA' });
        });
    });

    describe('empty params', () => {
        it('returns empty when no displayName and no parameters', () => {
            const result = summarizeInvocation({ parameters: [] });
            expect(result).toEqual({ kind: 'empty', text: 'Invocation' });
        });
    });

    describe('inline', () => {
        it('renders a single param inline', () => {
            const result = summarizeInvocation({ parameters: [{ customerId: '42' }] });
            expect(result).toEqual({ kind: 'inline', text: 'customerId: 42' });
        });

        it('renders 3 short params inline', () => {
            const result = summarizeInvocation({
                parameters: [{ a: '1' }, { b: '2' }, { c: '3' }],
            });
            expect(result).toEqual({ kind: 'inline', text: 'a: 1, b: 2, c: 3' });
        });

        it('falls back to chip when 3 params exceed 80 chars', () => {
            const long = 'x'.repeat(40);
            const result = summarizeInvocation({
                parameters: [{ a: long }, { b: long }, { c: long }],
            });
            expect(result).toEqual({ kind: 'chip', count: 3 });
        });
    });

    describe('chip', () => {
        it('returns chip for 4 short params regardless of length', () => {
            const result = summarizeInvocation({
                parameters: [{ a: '1' }, { b: '2' }, { c: '3' }, { d: '4' }],
            });
            expect(result).toEqual({ kind: 'chip', count: 4 });
        });

        it('returns chip for 2 params totalling over 80 chars', () => {
            const long = 'y'.repeat(50);
            const result = summarizeInvocation({
                parameters: [{ a: long }, { b: long }],
            });
            expect(result).toEqual({ kind: 'chip', count: 2 });
        });
    });
});
