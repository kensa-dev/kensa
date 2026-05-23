import { describe, it, expect } from 'vitest';
import { buildTree, expandProjectChildren } from './treeUtils';
import { collectFolderIds } from '@/state/treeExpansion';
import { Indices } from '@/types/Index';

describe('buildTree tag propagation', () => {
    const indices: Indices = [
        {
            id: 'a.b.FooTest',
            displayName: 'FooTest',
            testClass: 'a.b.FooTest',
            state: 'Passed',
            tags: ['slow', 'integration'],
        },
    ];

    it('carries tags on the leaf node', () => {
        const tree = buildTree(indices, 'Full');
        const root = tree[0];
        const pkgB = root.children!.find(c => c.displayName === 'b')!;
        const leaf = pkgB.children!.find(c => c.displayName === 'FooTest')!;
        expect(leaf.tags).toEqual(['slow', 'integration']);
    });

    it('leaves package nodes without tags', () => {
        const tree = buildTree(indices, 'Full');
        const root = tree[0];
        expect(root.tags).toBeUndefined();
        const pkgB = root.children!.find(c => c.displayName === 'b')!;
        expect(pkgB.tags).toBeUndefined();
    });
});

describe('expandProjectChildren', () => {
    const project = (children: Indices): Indices => [{
        id: 'src:demo',
        type: 'project',
        displayName: 'demo',
        testClass: '',
        state: 'Passed',
        children,
    }];

    it('builds the package tree under each project root', () => {
        const tree = expandProjectChildren(
            project([
                { id: 'foo.bar.FooTest', displayName: 'FooTest', testClass: 'foo.bar.FooTest', state: 'Passed' },
            ]),
            'Full',
        );
        const root = tree[0];
        expect(root.children![0].type).toBe('package');
        expect(root.children![0].displayName).toBe('foo');
    });

    it('preserves system-view children at the front of project children', () => {
        const tree = expandProjectChildren(
            project([
                { id: 'sv:1', type: 'system-view', displayName: 'System View', testClass: '', state: 'Passed' },
                { id: 'foo.FooTest', displayName: 'FooTest', testClass: 'foo.FooTest', state: 'Passed' },
            ]),
            'Full',
        );
        expect(tree[0].children![0].type).toBe('system-view');
    });

    // Regression test for collapse-all only collapsing project roots: the
    // package nodes were built per-render inside RecursiveMenuItem so their
    // 'pkg:*' ids never reached collectFolderIds. Pre-expanding here is what
    // makes collapse-all behave like IntelliJ.
    it('exposes pkg:* ids to collectFolderIds', () => {
        const tree = expandProjectChildren(
            project([
                { id: 'foo.bar.FooTest', displayName: 'FooTest', testClass: 'foo.bar.FooTest', state: 'Passed' },
            ]),
            'Full',
        );
        const ids = collectFolderIds(tree);
        expect(ids).toContain('src:demo');
        expect(ids).toContain('pkg:foo');
        expect(ids).toContain('pkg:foo.bar');
    });

    it('passes non-project roots through unchanged', () => {
        const raw: Indices = [
            { id: 'standalone', displayName: 'standalone', testClass: 'standalone', state: 'Passed' },
        ];
        expect(expandProjectChildren(raw, 'Full')).toEqual(raw);
    });
});
