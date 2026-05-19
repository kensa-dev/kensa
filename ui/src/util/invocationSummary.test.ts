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
