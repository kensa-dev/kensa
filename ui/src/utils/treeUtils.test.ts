import { describe, it, expect } from 'vitest';
import { buildTree } from './treeUtils';
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
