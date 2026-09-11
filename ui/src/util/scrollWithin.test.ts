import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {findScrollContainer, scrollWithin} from './scrollWithin';

// The vitest environment is node, so the DOM is stubbed with plain objects carrying
// only what scrollWithin reads. The point of these tests is the contract: the element
// is brought into view by scrolling its own container or the window, never by
// Element.scrollIntoView, which escapes a cross-origin iframe in Firefox.

type FakeEl = {
    parentElement: FakeEl | null;
    overflowY: string;
    scrollHeight: number;
    clientHeight: number;
    scrollTop: number;
    top: number;
    height: number;
    scrollTo: ReturnType<typeof vi.fn>;
    scrollIntoView: ReturnType<typeof vi.fn>;
    getBoundingClientRect: () => {top: number; height: number};
};

const el = (over: Partial<FakeEl> = {}): FakeEl => {
    const e: FakeEl = {
        parentElement: null,
        overflowY: 'visible',
        scrollHeight: 0,
        clientHeight: 0,
        scrollTop: 0,
        top: 0,
        height: 0,
        scrollTo: vi.fn(),
        scrollIntoView: vi.fn(),
        getBoundingClientRect: () => ({top: e.top, height: e.height}),
        ...over,
    };
    return e;
};

const body = el();
const windowScrollTo = vi.fn();

beforeEach(() => {
    vi.stubGlobal('document', {body});
    vi.stubGlobal('getComputedStyle', (n: FakeEl) => ({overflowY: n.overflowY}));
    vi.stubGlobal('window', {scrollY: 0, innerHeight: 800, scrollTo: windowScrollTo});
    windowScrollTo.mockClear();
});

afterEach(() => vi.unstubAllGlobals());

describe('findScrollContainer', () => {
    it('finds the nearest ancestor that scrolls', () => {
        const pane = el({overflowY: 'auto', scrollHeight: 3000, clientHeight: 700, parentElement: body});
        const wrapper = el({parentElement: pane});
        const card = el({parentElement: wrapper});
        expect(findScrollContainer(card as unknown as Element)).toBe(pane);
    });

    it('ignores overflow:auto ancestors with nothing to scroll', () => {
        const pane = el({overflowY: 'auto', scrollHeight: 700, clientHeight: 700, parentElement: body});
        const card = el({parentElement: pane});
        expect(findScrollContainer(card as unknown as Element)).toBeNull();
    });
});

describe('scrollWithin', () => {
    it('scrolls the container so the element starts at its top, and never calls scrollIntoView', () => {
        const pane = el({overflowY: 'auto', scrollHeight: 3000, clientHeight: 700, scrollTop: 100, top: 50, parentElement: body});
        const card = el({parentElement: pane, top: 650, height: 400});
        scrollWithin(card as unknown as Element, {block: 'start'});
        // 650 (card top in viewport) - 50 (pane top) + 100 (already scrolled) = 700 from pane top
        expect(pane.scrollTo).toHaveBeenCalledWith({top: 700, behavior: 'smooth'});
        expect(card.scrollIntoView).not.toHaveBeenCalled();
        expect(windowScrollTo).not.toHaveBeenCalled();
    });

    it('with block nearest, leaves an element already in view alone', () => {
        const pane = el({overflowY: 'auto', scrollHeight: 3000, clientHeight: 700, scrollTop: 100, top: 0, parentElement: body});
        const item = el({parentElement: pane, top: 200, height: 40});
        scrollWithin(item as unknown as Element, {block: 'nearest', behavior: 'auto'});
        expect(pane.scrollTo).not.toHaveBeenCalled();
    });

    it('with block nearest, scrolls just far enough to reveal an element below the fold', () => {
        const pane = el({overflowY: 'auto', scrollHeight: 3000, clientHeight: 700, scrollTop: 0, top: 0, parentElement: body});
        const item = el({parentElement: pane, top: 900, height: 40});
        scrollWithin(item as unknown as Element, {block: 'nearest', behavior: 'auto'});
        // offsetTop 900 + height 40 - clientHeight 700 = 240
        expect(pane.scrollTo).toHaveBeenCalledWith({top: 240, behavior: 'auto'});
    });

    it('falls back to the window when nothing between the element and body scrolls', () => {
        const card = el({parentElement: body, top: 1200, height: 300});
        scrollWithin(card as unknown as Element, {block: 'start'});
        expect(windowScrollTo).toHaveBeenCalledWith({top: 1200, behavior: 'smooth'});
        expect(card.scrollIntoView).not.toHaveBeenCalled();
    });
});
