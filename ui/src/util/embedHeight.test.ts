import {describe, expect, it} from 'vitest';
import {heightMessage, isHeightMessage, nextHeightPost} from './embedHeight';

describe('heightMessage', () => {
    it('builds the kensa:height message', () => {
        expect(heightMessage(812)).toEqual({type: 'kensa:height', height: 812});
    });
});

describe('isHeightMessage', () => {
    it('accepts the message it builds', () => {
        expect(isHeightMessage(heightMessage(812))).toBe(true);
        expect(isHeightMessage(heightMessage(0))).toBe(true);
    });

    it('rejects other types', () => {
        expect(isHeightMessage({type: 'other', height: 1})).toBe(false);
    });

    it('rejects a missing, negative, infinite or non-numeric height', () => {
        expect(isHeightMessage({type: 'kensa:height'})).toBe(false);
        expect(isHeightMessage({type: 'kensa:height', height: -1})).toBe(false);
        expect(isHeightMessage({type: 'kensa:height', height: Infinity})).toBe(false);
        expect(isHeightMessage({type: 'kensa:height', height: NaN})).toBe(false);
        expect(isHeightMessage({type: 'kensa:height', height: '812'})).toBe(false);
    });

    it('rejects non-objects', () => {
        expect(isHeightMessage(null)).toBe(false);
        expect(isHeightMessage('kensa:height')).toBe(false);
        expect(isHeightMessage(undefined)).toBe(false);
    });
});

describe('nextHeightPost', () => {
    it('posts the first height', () => {
        expect(nextHeightPost(null, 812)).toEqual(heightMessage(812));
    });

    it('posts a changed height', () => {
        expect(nextHeightPost(812, 900)).toEqual(heightMessage(900));
    });

    it('posts nothing when the height is unchanged', () => {
        expect(nextHeightPost(812, 812)).toBeNull();
    });

    it('rounds up so the frame never clips a fractional pixel', () => {
        expect(nextHeightPost(null, 812.2)).toEqual(heightMessage(813));
        expect(nextHeightPost(813, 812.2)).toBeNull();
    });
});
