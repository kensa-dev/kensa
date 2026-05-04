import {describe, expect, it} from 'vitest';
import {injectBaseHref} from './injectBaseHref';

describe('injectBaseHref', () => {
    const base = 'https://example.com/sources/uiTest/';

    it('returns content unchanged when a <base> tag is already present', () => {
        const html = '<html><head><base href="x"></head><body></body></html>';
        expect(injectBaseHref(html, base)).toBe(html);
    });

    it('injects base inside <head> when present', () => {
        const html = '<html><head><meta charset="utf-8"></head><body></body></html>';
        expect(injectBaseHref(html, base)).toBe(
            `<html><head><base href="${base}"><meta charset="utf-8"></head><body></body></html>`
        );
    });

    it('inserts a <head> with base when <html> has no <head>', () => {
        const html = '<html><body><img src="tabs/x.png"></body></html>';
        expect(injectBaseHref(html, base)).toBe(
            `<html><head><base href="${base}"></head><body><img src="tabs/x.png"></body></html>`
        );
    });

    it('prepends base for headless fragment content so the iframe parser puts it in the implicit head', () => {
        const html = '<div><img src="tabs/x.png"></div>';
        expect(injectBaseHref(html, base)).toBe(
            `<base href="${base}"><div><img src="tabs/x.png"></div>`
        );
    });

    it('matches case-insensitively on existing tags', () => {
        const html = '<HTML><BODY><img src="tabs/x.png"></BODY></HTML>';
        expect(injectBaseHref(html, base)).toBe(
            `<HTML><head><base href="${base}"></head><BODY><img src="tabs/x.png"></BODY></HTML>`
        );
    });
});
