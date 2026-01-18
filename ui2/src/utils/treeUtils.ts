import {Indices} from "@/types/Index";

export const buildTree = (indices: Indices): Indices => {
    const root: Indices = [];

    indices.forEach((idx) => {
        // Only build package trees for nodes that have a testClass
        if (!idx.testClass) return;

        const parts = idx.testClass.split('.');
        let currentLevel = root;

        parts.forEach((part: string, i: number) => {
            const isLast = i === parts.length - 1;
            const fullName = parts.slice(0, i + 1).join('.');

            let existing = currentLevel.find((node) => node.displayName === part);

            if (!existing) {
                existing = {
                    id: isLast ? idx.id : `pkg:${fullName}`,
                    displayName: part,
                    testClass: isLast ? idx.testClass : fullName,
                    type: isLast ? 'test' : 'package',
                    children: [],
                    state: idx.state,
                };
                currentLevel.push(existing);
            }

            // Propagate failure state up the tree
            if (idx.state === 'Failed') {
                existing.state = 'Failed';
            }

            currentLevel = existing.children!;
        });
    });

    return root;
};