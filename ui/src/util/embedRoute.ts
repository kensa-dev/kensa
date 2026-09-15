import {Index, Indices} from '@/types/Index';

export interface EmbedRoute {
    testId: string;
}

const ROUTE_PATTERN = /^\/embed\/(.+)$/;

export function parseEmbedRoute(pathname: string): EmbedRoute | null {
    const match = pathname.match(ROUTE_PATTERN);
    if (!match) return null;
    const [, id] = match;
    let testId = id;
    try {
        testId = decodeURIComponent(id);
    } catch {
        testId = id;
    }
    return {testId};
}

export type EmbedTheme = 'light' | 'dark' | 'auto';

export interface EmbedParams {
    method: string | null;
    invocation: number;
    theme: EmbedTheme;
    notes: boolean;
}

export function embedParams(search: URLSearchParams): EmbedParams {
    const theme = search.get('theme');
    const invocationParam = search.get('invocation');
    const invocation = invocationParam === null ? -1 : Number(invocationParam);
    const notes = search.get('notes');
    return {
        method: search.get('method'),
        invocation: Number.isInteger(invocation) && invocation >= 0 ? invocation : -1,
        theme: theme === 'light' || theme === 'dark' ? theme : 'auto',
        notes: notes === '1' || notes === 'true',
    };
}

export type EmbedTarget = {found: true} | {found: false; missing: 'test' | 'method'};

const findNode = (nodes: Indices, id: string): Index | null => {
    for (const node of nodes) {
        if (node.id === id) return node;
        const found = node.children ? findNode(node.children, id) : null;
        if (found) return found;
    }
    return null;
};

const normalise = (s: string) => s.trim().toLowerCase();

// Matches the way TestContainer picks the method to expand, so a link that
// resolves here also expands there.
export function embedTarget(indices: Indices, testId: string, method: string | null): EmbedTarget {
    const node = findNode(indices, testId);
    if (!node) return {found: false, missing: 'test'};
    if (method === null) return {found: true};
    const target = normalise(method);
    const match = (node.children ?? []).some(child =>
        [child.testMethod, child.displayName].filter(Boolean).map(x => normalise(String(x))).includes(target));
    return match ? {found: true} : {found: false, missing: 'method'};
}
