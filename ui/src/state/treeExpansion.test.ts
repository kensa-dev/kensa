import { describe, it, expect } from 'vitest';
import { Indices } from '@/types/Index';
import {
    collapseAll,
    collectFolderIds,
    EMPTY,
    expandAll,
    fromPersisted,
    isCollapsed,
    setCollapsed,
    toPersisted,
} from './treeExpansion';

describe('isCollapsed', () => {
    it('returns false for an unknown id (default-expanded)', () => {
        expect(isCollapsed(EMPTY, 'pkg:a')).toBe(false);
    });

    it('returns true for an id present in the collapsed set', () => {
        const state = setCollapsed(EMPTY, 'pkg:a', true);
        expect(isCollapsed(state, 'pkg:a')).toBe(true);
    });
});

describe('setCollapsed', () => {
    it('adds the id to the set when collapsed=true', () => {
        const next = setCollapsed(EMPTY, 'pkg:a', true);
        expect(next.collapsed.has('pkg:a')).toBe(true);
    });

    it('returns a new instance and leaves the original untouched', () => {
        const original = EMPTY;
        const next = setCollapsed(original, 'pkg:a', true);
        expect(next).not.toBe(original);
        expect(original.collapsed.has('pkg:a')).toBe(false);
    });

    it('removes the id from the set when collapsed=false', () => {
        const collapsed = setCollapsed(EMPTY, 'pkg:a', true);
        const expanded = setCollapsed(collapsed, 'pkg:a', false);
        expect(expanded.collapsed.has('pkg:a')).toBe(false);
    });

    it('adding an already-collapsed id yields equal content', () => {
        const a = setCollapsed(EMPTY, 'pkg:a', true);
        const b = setCollapsed(a, 'pkg:a', true);
        expect([...b.collapsed].sort()).toEqual(['pkg:a']);
    });

    it('removing a missing id yields equal content', () => {
        const a = setCollapsed(EMPTY, 'pkg:a', false);
        expect([...a.collapsed]).toEqual([]);
    });
});

describe('collectFolderIds', () => {
    it('returns [] for an empty tree', () => {
        expect(collectFolderIds([])).toEqual([]);
    });

    it('includes a single project root with no children', () => {
        const nodes: Indices = [
            { id: 'src:a', displayName: 'a', testClass: '', state: 'Passed', type: 'project' },
        ];
        expect(collectFolderIds(nodes)).toEqual(['src:a']);
    });

    it('includes project then package in tree order', () => {
        const nodes: Indices = [
            {
                id: 'src:a',
                displayName: 'a',
                testClass: '',
                state: 'Passed',
                type: 'project',
                children: [
                    { id: 'pkg:a.b', displayName: 'b', testClass: '', state: 'Passed', type: 'package' },
                ],
            },
        ];
        expect(collectFolderIds(nodes)).toEqual(['src:a', 'pkg:a.b']);
    });

    it('excludes test and system-view nodes', () => {
        const nodes: Indices = [
            {
                id: 'src:a',
                displayName: 'a',
                testClass: '',
                state: 'Passed',
                type: 'project',
                children: [
                    { id: 'a.b.FooTest', displayName: 'FooTest', testClass: 'a.b.FooTest', state: 'Passed', type: 'test' },
                    { id: 'view:1', displayName: 'view', testClass: '', state: 'Passed', type: 'system-view' },
                ],
            },
        ];
        expect(collectFolderIds(nodes)).toEqual(['src:a']);
    });

    it('includes deeply nested package folders', () => {
        const nodes: Indices = [
            {
                id: 'src:a',
                displayName: 'a',
                testClass: '',
                state: 'Passed',
                type: 'project',
                children: [
                    {
                        id: 'pkg:a.b',
                        displayName: 'b',
                        testClass: '',
                        state: 'Passed',
                        type: 'package',
                        children: [
                            { id: 'pkg:a.b.c', displayName: 'c', testClass: '', state: 'Passed', type: 'package' },
                        ],
                    },
                ],
            },
        ];
        expect(collectFolderIds(nodes)).toEqual(['src:a', 'pkg:a.b', 'pkg:a.b.c']);
    });

    it('dedupes ids that appear more than once', () => {
        const nodes: Indices = [
            { id: 'src:a', displayName: 'a', testClass: '', state: 'Passed', type: 'project' },
            { id: 'src:a', displayName: 'a', testClass: '', state: 'Passed', type: 'project' },
        ];
        expect(collectFolderIds(nodes)).toEqual(['src:a']);
    });
});

describe('expandAll / collapseAll', () => {
    const tree: Indices = [
        {
            id: 'src:a',
            displayName: 'a',
            testClass: '',
            state: 'Passed',
            type: 'project',
            children: [
                {
                    id: 'pkg:a.b',
                    displayName: 'b',
                    testClass: '',
                    state: 'Passed',
                    type: 'package',
                    children: [
                        { id: 'pkg:a.b.c', displayName: 'c', testClass: '', state: 'Passed', type: 'package' },
                    ],
                },
            ],
        },
    ];

    it('expandAll on a fully-collapsed state yields an empty set', () => {
        const collapsed = collapseAll(EMPTY, tree);
        const expanded = expandAll(collapsed, tree);
        expect([...expanded.collapsed]).toEqual([]);
    });

    it('expandAll is idempotent', () => {
        const once = expandAll(EMPTY, tree);
        const twice = expandAll(once, tree);
        expect([...twice.collapsed]).toEqual([]);
    });

    it('collapseAll collapses every folder id including project roots', () => {
        const next = collapseAll(EMPTY, tree);
        expect(next.collapsed.has('src:a')).toBe(true);
        expect(next.collapsed.has('pkg:a.b')).toBe(true);
        expect(next.collapsed.has('pkg:a.b.c')).toBe(true);
    });

    it('collapseAll preserves pre-existing collapsed ids that are not in the current tree', () => {
        const seed = setCollapsed(EMPTY, 'pkg:foreign', true);
        const next = collapseAll(seed, tree);
        expect(next.collapsed.has('pkg:foreign')).toBe(true);
    });
});

describe('toPersisted / fromPersisted', () => {
    it('toPersisted returns version 1 with collapsed ids sorted', () => {
        const state = setCollapsed(setCollapsed(EMPTY, 'pkg:b', true), 'pkg:a', true);
        expect(toPersisted(state)).toEqual({ version: 1, collapsed: ['pkg:a', 'pkg:b'] });
    });

    it('round-trips a state', () => {
        const state = setCollapsed(setCollapsed(EMPTY, 'pkg:b', true), 'pkg:a', true);
        const restored = fromPersisted(toPersisted(state));
        expect([...restored.collapsed].sort()).toEqual(['pkg:a', 'pkg:b']);
    });

    it('returns EMPTY for null', () => {
        expect([...fromPersisted(null).collapsed]).toEqual([]);
    });

    it('returns EMPTY for an unknown version', () => {
        expect([...fromPersisted({ version: 2, collapsed: ['pkg:a'] }).collapsed]).toEqual([]);
    });

    it('returns EMPTY when collapsed is not an array', () => {
        expect([...fromPersisted({ version: 1, collapsed: 'oops' }).collapsed]).toEqual([]);
    });

    it('rehydrates collapsed strings into the set', () => {
        const restored = fromPersisted({ version: 1, collapsed: ['a', 'b'] });
        expect([...restored.collapsed].sort()).toEqual(['a', 'b']);
    });
});
