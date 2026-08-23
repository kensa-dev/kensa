import {describe, expect, it} from 'vitest';
import {formatMs} from './formatMs';

describe('formatMs', () => {
    it('formats sub-second, seconds and minutes', () => {
        expect(formatMs(42)).toBe('42ms');
        expect(formatMs(1500)).toBe('1.5s');
        expect(formatMs(48200)).toBe('48.2s');
        expect(formatMs(102000)).toBe('1m 42s');
        expect(formatMs(3_725_000)).toBe('1h 2m');
    });

    it('returns a dash for non-finite input', () => {
        expect(formatMs(Number.NaN)).toBe('–');
        expect(formatMs(Number.POSITIVE_INFINITY)).toBe('–');
    });
});
