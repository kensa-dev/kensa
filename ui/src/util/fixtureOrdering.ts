import { FixtureSpec, NameAndValues } from "@/types/Test";

export interface OrderedFixtureRow {
    key: string;
    value: string;
    depth: number;
}

function groupByFamily(fixtures: NameAndValues, specs: FixtureSpec[]): NameAndValues {
    const fixtureMap = new Map(fixtures.map(row => [Object.keys(row)[0], row]));
    const specMap = new Map(specs.map(s => [s.key, s]));

    const childrenOf = new Map<string, string[]>();
    for (const spec of specs) {
        for (const parent of spec.parents) {
            if (!childrenOf.has(parent)) childrenOf.set(parent, []);
            childrenOf.get(parent)!.push(spec.key);
        }
    }

    const result: NameAndValues = [];
    const emitted = new Set<string>();

    const tryEmit = (key: string) => {
        if (emitted.has(key) || !fixtureMap.has(key)) return;
        const spec = specMap.get(key);
        if (spec?.parents.some(p => fixtureMap.has(p) && !emitted.has(p))) return;
        emitted.add(key);
        result.push(fixtureMap.get(key)!);
        for (const child of (childrenOf.get(key) ?? [])) tryEmit(child);
    };

    for (const [key] of fixtureMap) tryEmit(key);
    return result;
}

function getDepth(key: string, specs: FixtureSpec[], visited = new Set<string>()): number {
    if (visited.has(key)) return 0;
    const spec = specs.find(s => s.key === key);
    if (!spec || spec.parents.length === 0) return 0;
    visited.add(key);
    return 1 + Math.max(...spec.parents.map(p => getDepth(p, specs, new Set(visited))));
}

export function orderedFixtureRows(
    fixtures: NameAndValues = [],
    fixtureSpecs: FixtureSpec[] = [],
): OrderedFixtureRow[] {
    const hasHierarchy = fixtureSpecs.some(s => s.parents.length > 0);
    const ordered = hasHierarchy ? groupByFamily(fixtures, fixtureSpecs) : fixtures;
    return ordered.map(row => {
        const [key, value] = Object.entries(row)[0];
        return { key, value, depth: hasHierarchy ? getDepth(key, fixtureSpecs) : 0 };
    });
}
