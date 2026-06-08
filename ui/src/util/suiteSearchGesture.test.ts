import { describe, it, expect } from 'vitest';
import { resolveGesture } from './suiteSearchGesture';

describe('resolveGesture', () => {
    it('resolves to fixture when meta is held', () => {
        expect(resolveGesture({ metaKey: true, ctrlKey: false })).toBe('fixture');
    });

    it('resolves to fixture when ctrl is held', () => {
        expect(resolveGesture({ metaKey: false, ctrlKey: true })).toBe('fixture');
    });

    it('resolves to none when no modifier is held', () => {
        expect(resolveGesture({ metaKey: false, ctrlKey: false })).toBe('none');
    });
});
