import {describe, expect, it} from 'vitest';
import {embedLinkFor} from './embedLink';

const location = {origin: 'http://localhost:5173', pathname: '/index.html', search: ''};
const published = {linkBaseUrl: 'https://reports.example.com/latest/index.html', unfurl: true};

describe('embedLinkFor', () => {
    it('copies the hash link when the report has no unfurl pages', () => {
        expect(embedLinkFor({linkBaseUrl: 'https://reports.example.com/latest/index.html'}, location, '.', 'default::com.example.OrderTest', 'orderIsAccepted'))
            .toBe('https://reports.example.com/latest/index.html#/embed/default::com.example.OrderTest?method=orderIsAccepted');
        expect(embedLinkFor({}, location, '.', 'default::com.example.OrderTest', 'orderIsAccepted'))
            .toBe('http://localhost:5173/index.html#/embed/default::com.example.OrderTest?method=orderIsAccepted');
    });

    it('copies the method page beside the report when it has unfurl pages', () => {
        expect(embedLinkFor(published, location, '.', 'default::com.example.OrderTest', 'orderIsAccepted'))
            .toBe('https://reports.example.com/latest/embed/com.example.OrderTest/orderIsAccepted.html');
    });

    it('copies the class page for a class link', () => {
        expect(embedLinkFor(published, location, '.', 'default::com.example.OrderTest'))
            .toBe('https://reports.example.com/latest/embed/com.example.OrderTest/index.html');
    });

    it('encodes a method name with spaces', () => {
        expect(embedLinkFor(published, location, '.', 'default::com.example.OrderTest', 'order is rejected'))
            .toBe('https://reports.example.com/latest/embed/com.example.OrderTest/order%20is%20rejected.html');
    });

    it('resolves a directory base the way reportBase does', () => {
        expect(embedLinkFor({linkBaseUrl: 'https://reports.example.com/latest/', unfurl: true}, location, '.', 'default::com.example.OrderTest'))
            .toBe('https://reports.example.com/latest/embed/com.example.OrderTest/index.html');
    });

    it('goes through the source bundle of a site', () => {
        expect(embedLinkFor(published, location, 'sources/uiTest', 'uiTest::com.example.OrderTest', 'orderIsAccepted'))
            .toBe('https://reports.example.com/latest/sources/uiTest/embed/com.example.OrderTest/orderIsAccepted.html');
        expect(embedLinkFor(published, location, './sources/uiTest/', 'uiTest::com.example.OrderTest'))
            .toBe('https://reports.example.com/latest/sources/uiTest/embed/com.example.OrderTest/index.html');
    });

    it('keeps the hash link for an invocation, which has no page of its own', () => {
        expect(embedLinkFor(published, location, '.', 'default::com.example.OrderTest', 'orderIsAccepted', 1))
            .toBe('https://reports.example.com/latest/index.html#/embed/default::com.example.OrderTest?method=orderIsAccepted&invocation=1');
    });
});
