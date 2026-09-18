import {describe, expect, it} from 'vitest';
import {customTabsOf} from './customTabs';
import {Invocation} from '@/types/Test';

const baseInvocation: Invocation = {
    state: 'Passed',
    displayName: 'a test',
    elapsedTime: '1ms',
    highlights: [],
    sentences: [],
    parameters: [],
    givens: [],
    capturedInteractions: [],
    capturedOutputs: [],
    fixtures: [],
    fixtureSpecs: [],
    executionException: {},
};

describe('customTabsOf', () => {
    it('includes a tab with a file, not empty', () => {
        const invocation: Invocation = {
            ...baseInvocation,
            customTabContents: [
                {tabId: 'logs', label: 'Logs', file: 'logs.txt', mediaType: 'text/plain', entries: 3},
            ],
        };

        expect(customTabsOf(invocation)).toEqual([
            {id: 'logs', label: 'Logs', file: 'logs.txt', mediaType: 'text/plain', empty: false, kind: 'custom'},
        ]);
    });

    it('includes a zero-entry tab with no file as empty', () => {
        const invocation: Invocation = {
            ...baseInvocation,
            customTabContents: [
                {tabId: 'logs', label: 'Logs', entries: 0},
            ],
        };

        expect(customTabsOf(invocation)).toEqual([
            {id: 'logs', label: 'Logs', file: undefined, mediaType: 'text/plain', empty: true, kind: 'custom'},
        ]);
    });

    it('drops a skipped tab with neither file nor entries', () => {
        const invocation: Invocation = {
            ...baseInvocation,
            customTabContents: [
                {tabId: 'logs', label: 'Logs', visibility: 'OnlyOnFailure'},
            ],
        };

        expect(customTabsOf(invocation)).toEqual([]);
    });

    it('defaults mediaType to text/plain when absent', () => {
        const invocation: Invocation = {
            ...baseInvocation,
            customTabContents: [
                {tabId: 'logs', label: 'Logs', file: 'logs.txt'},
            ],
        };

        expect(customTabsOf(invocation)[0].mediaType).toBe('text/plain');
    });

    it('returns an empty list when there are no custom tab contents', () => {
        expect(customTabsOf(baseInvocation)).toEqual([]);
    });
});
