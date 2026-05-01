import {Index, Indices} from "@/types/Index";

const findCommonPackage = (fqcns: string[]): string => {
    if (fqcns.length === 0) return "";
    const packages = fqcns.map(fqcn => fqcn.split('.').slice(0, -1));
    const shortest = packages.reduce((a, b) => a.length <= b.length ? a : b);
    let common = 0;
    for (let i = 0; i < shortest.length; i++) {
        if (packages.every(p => p[i] === shortest[i])) common++;
        else break;
    }
    return shortest.slice(0, common).join('.');
};

const buildPackageTree = (indices: Indices, commonBase: string): Indices => {
    const root: Indices = [];

    indices.forEach((idx) => {
        if (!idx.testClass) return;

        const stripped = commonBase && idx.testClass.startsWith(commonBase + '.')
            ? idx.testClass.slice(commonBase.length + 1)
            : idx.testClass;

        const parts = stripped.split('.');
        let currentLevel = root;

        parts.forEach((part: string, i: number) => {
            const isLast = i === parts.length - 1;
            const fullName = commonBase
                ? commonBase + '.' + parts.slice(0, i + 1).join('.')
                : parts.slice(0, i + 1).join('.');

            let existing = currentLevel.find((node) => node.displayName === part);

            if (!existing) {
                existing = {
                    id: isLast ? idx.id : `pkg:${fullName}`,
                    displayName: part,
                    testClass: isLast ? idx.testClass : fullName,
                    type: isLast ? 'test' : 'package',
                    children: isLast ? idx.children : [],
                    state: idx.state,
                    issues: isLast ? idx.issues : undefined,
                    hasErrors: isLast ? idx.hasErrors : undefined,
                };
                currentLevel.push(existing);
            }

            if (idx.state === 'Failed') existing.state = 'Failed';
            else if (idx.state === 'Passed' && existing.state !== 'Failed') existing.state = 'Passed';

            if (!isLast) currentLevel = existing.children!;
        });
    });

    return root;
};

const buildFlatTree = (indices: Indices): Indices =>
    indices
        .filter(idx => idx.testClass)
        .map((idx): Index => ({
            ...idx,
            displayName: idx.testClass.split('.').pop() ?? idx.displayName,
        }));

export const buildTree = (indices: Indices, packageDisplay: string, packageDisplayRoot?: string): Indices => {
    if (packageDisplay === 'Hidden') return buildFlatTree(indices);

    const commonBase = packageDisplay === 'HideCommonPackages'
        ? (packageDisplayRoot ?? findCommonPackage(indices.filter(i => i.testClass).map(i => i.testClass)))
        : "";

    return buildPackageTree(indices, commonBase);
};
