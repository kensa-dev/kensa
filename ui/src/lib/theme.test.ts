import {describe, expect, it} from 'vitest';
import {effective, initialTheme, next, parseStored, switchLabel} from './theme';

describe('parseStored', () => {
    it('accepts only the two choices', () => {
        expect(parseStored('light')).toBe('light');
        expect(parseStored('dark')).toBe('dark');
        expect(parseStored(null)).toBeNull();
        expect(parseStored('')).toBeNull();
        expect(parseStored('auto')).toBeNull();
    });
});

describe('effective', () => {
    it('follows the OS until a choice is made', () => {
        expect(effective(null, true)).toBe('dark');
        expect(effective(null, false)).toBe('light');
    });

    it('a stored choice wins over the OS', () => {
        expect(effective('light', true)).toBe('light');
        expect(effective('dark', false)).toBe('dark');
    });
});

describe('next', () => {
    it('flips between the two states', () => {
        expect(next('light')).toBe('dark');
        expect(next('dark')).toBe('light');
    });
});

describe('switchLabel', () => {
    it('says where pressing goes', () => {
        expect(switchLabel('light')).toBe('Switch to dark');
        expect(switchLabel('dark')).toBe('Switch to light');
    });
});

describe('initialTheme', () => {
    it.each([
        ['plain page, nothing stored, OS dark', {embed: false, embedTheme: null, themeParam: null, stored: null, osDark: true}, 'dark'],
        ['plain page, nothing stored, OS light', {embed: false, embedTheme: null, themeParam: null, stored: null, osDark: false}, 'light'],
        ['plain page, stored light, OS dark', {embed: false, embedTheme: null, themeParam: null, stored: 'light', osDark: true}, 'light'],
        ['?theme=light with stored dark', {embed: false, embedTheme: null, themeParam: 'light', stored: 'dark', osDark: true}, 'light'],
        ['?theme=darkness with stored dark falls through to stored', {embed: false, embedTheme: null, themeParam: 'darkness', stored: 'dark', osDark: false}, 'dark'],
        ['embed auto with stored dark and OS light', {embed: true, embedTheme: 'auto', themeParam: null, stored: 'dark', osDark: false}, 'light'],
        ['embed dark with OS light', {embed: true, embedTheme: 'dark', themeParam: null, stored: null, osDark: false}, 'dark'],
        ['embed light with ?theme=dark', {embed: true, embedTheme: 'light', themeParam: 'dark', stored: null, osDark: true}, 'light'],
    ] as const)('%s', (_name, inputs, expected) => {
        expect(initialTheme(inputs)).toBe(expected);
    });
});
