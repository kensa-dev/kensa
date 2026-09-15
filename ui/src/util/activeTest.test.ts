import {describe, expect, it} from 'vitest';
import {activeIndex, applyEntries, entryToCrossing} from './activeTest';

describe('applyEntries', () => {
    it('adds the indices whose header is above the line', () => {
        expect(applyEntries(new Set(), [{index: 0, above: true}, {index: 1, above: true}])).toEqual(new Set([0, 1]));
    });

    it('removes the indices whose header is back below the line', () => {
        expect(applyEntries(new Set([0, 1, 2]), [{index: 2, above: false}, {index: 1, above: false}])).toEqual(new Set([0]));
    });

    it('leaves unmentioned indices alone', () => {
        expect(applyEntries(new Set([0]), [{index: 3, above: true}])).toEqual(new Set([0, 3]));
    });

    it('does not mutate the input set', () => {
        const crossed = new Set([0]);
        applyEntries(crossed, [{index: 1, above: true}, {index: 0, above: false}]);
        expect(crossed).toEqual(new Set([0]));
    });
});

describe('activeIndex', () => {
    it('is the highest crossed index', () => {
        expect(activeIndex(new Set([2, 0, 1]))).toBe(2);
    });

    it('is -1 when nothing has crossed', () => {
        expect(activeIndex(new Set())).toBe(-1);
    });
});

describe('entryToCrossing', () => {
    const indexOf = (target: string) => ['a', 'b', 'c'].indexOf(target);

    it('marks a header whose top is above the root top as above', () => {
        expect(entryToCrossing({target: 'b', boundingClientRect: {top: 90}, rootBounds: {top: 100}}, indexOf)).toEqual({index: 1, above: true});
    });

    it('marks a header at or below the root top as not above', () => {
        expect(entryToCrossing({target: 'c', boundingClientRect: {top: 100}, rootBounds: {top: 100}}, indexOf)).toEqual({index: 2, above: false});
        expect(entryToCrossing({target: 'a', boundingClientRect: {top: 400}, rootBounds: {top: 100}}, indexOf)).toEqual({index: 0, above: false});
    });

    it('ignores targets that are no longer in the list', () => {
        expect(entryToCrossing({target: 'z', boundingClientRect: {top: 0}, rootBounds: {top: 100}}, indexOf)).toBeNull();
    });

    it('ignores entries without root bounds', () => {
        expect(entryToCrossing({target: 'a', boundingClientRect: {top: 0}, rootBounds: null}, indexOf)).toBeNull();
    });
});
