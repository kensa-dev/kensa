export interface TreeNode {
    id: string;
    name: string;
    type: 'package' | 'class';
    children: TreeNode[];
    state: string;
    fullName: string;
}

export const buildTree = (indices: any[]): TreeNode[] => {
    const root: TreeNode[] = [];

    indices.forEach((idx) => {
        const parts = idx.testClass.split('.');
        let currentLevel = root;

        parts.forEach((part: string, i: number) => {
            const isLast = i === parts.length - 1;
            const fullName = parts.slice(0, i + 1).join('.');
            let existing = currentLevel.find((node) => node.name === part);

            if (!existing) {
                existing = {
                    id: isLast ? idx.id : `pkg:${fullName}`, // Prefix packages
                    name: part,
                    type: isLast ? 'class' : 'package',
                    children: [],
                    state: idx.state,
                    fullName: isLast ? idx.testClass : fullName // Ensure this is stored
                };
                currentLevel.push(existing);
            }

            // If a child fails, the parent package also shows as failed
            if (idx.state === 'Failed') existing.state = 'Failed';

            currentLevel = existing.children;
        });
    });

    return root;
};