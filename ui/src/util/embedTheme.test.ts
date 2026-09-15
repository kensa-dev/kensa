import {describe, expect, it} from 'vitest';
import {resolveTheme} from './embedTheme';

describe('resolveTheme', () => {
    it('dark wins regardless of the OS preference', () => {
        expect(resolveTheme('dark', true)).toBe(true);
        expect(resolveTheme('dark', false)).toBe(true);
    });

    it('light wins regardless of the OS preference', () => {
        expect(resolveTheme('light', true)).toBe(false);
        expect(resolveTheme('light', false)).toBe(false);
    });

    it('auto follows the OS preference', () => {
        expect(resolveTheme('auto', true)).toBe(true);
        expect(resolveTheme('auto', false)).toBe(false);
    });
});
