import {describe, expect, it, vi} from 'vitest';
import {anchorHash, copyLink} from './anchorLink';

describe('anchorHash', () => {
    it('links a test class', () => {
        expect(anchorHash('a::x.FooTest')).toBe('#/test/a::x.FooTest');
    });

    it('links a test method', () => {
        expect(anchorHash('a::x.FooTest', 'myTest')).toBe('#/test/a::x.FooTest?method=myTest');
    });

    it('links an invocation, including the first', () => {
        expect(anchorHash('a::x.FooTest', 'myTest', 0)).toBe('#/test/a::x.FooTest?method=myTest&invocation=0');
    });

    it('encodes the method name', () => {
        expect(anchorHash('a::x.FooTest', 'my test')).toBe('#/test/a::x.FooTest?method=my+test');
    });
});

describe('copyLink', () => {
    it('writes the url to the clipboard', async () => {
        const writeText = vi.fn().mockResolvedValue(undefined);
        const warn = vi.fn();
        await copyLink('http://x/#/test/t', {writeText}, warn);
        expect(writeText).toHaveBeenCalledWith('http://x/#/test/t');
        expect(warn).not.toHaveBeenCalled();
    });

    it('warns with the url when there is no clipboard', async () => {
        const warn = vi.fn();
        await copyLink('http://x/#/test/t', undefined, warn);
        expect(warn).toHaveBeenCalledWith(expect.stringContaining('secure context'), 'http://x/#/test/t');
    });

    it('warns with the url when the clipboard write is rejected', async () => {
        const writeText = vi.fn().mockRejectedValue(new Error('denied'));
        const warn = vi.fn();
        await copyLink('http://x/#/test/t', {writeText}, warn);
        expect(warn).toHaveBeenCalledWith(expect.stringContaining('secure context'), 'http://x/#/test/t');
    });
});
