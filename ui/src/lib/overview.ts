import {Index, Indices} from '@/types/Index';
import {TestState} from '@/types/Test';

export interface StateCounts { passed: number; failed: number; disabled: number; notExecuted: number; total: number }
export interface GroupCounts extends StateCounts { key: string }
export interface IssueCounts extends StateCounts { issue: string }
export interface EpicGroup extends StateCounts { epic: string; issues: IssueCounts[]; epicOnly: number }
export interface FailureRow { nodeId: string; classId: string; testClass: string; testMethod: string; displayName: string; epics: string[]; issues: string[] }
export interface FailuresModel { rows: FailureRow[]; concentration: {pkg: string; count: number} | null }
export interface DurationBucket { label: string; count: number }
export interface SlowRow { nodeId: string; classId: string; testClass: string; testMethod: string; displayName: string; elapsedMs: number }
export interface GroupCount { key: string; count: number }
export interface DensityModel { assertionsPerTest: number; expandableShare: number; parameterised: number; weakest: {classId: string; testClass: string; assertionsPerTest: number} | null }

export interface OverviewModel {
    results: StateCounts | null;
    byTag: GroupCounts[] | null;
    byPackage: GroupCounts[] | null;
    byEpic: EpicGroup[] | null;
    failures: FailuresModel | null;
    durations: DurationBucket[] | null;
    slowest: SlowRow[] | null;
    participants: GroupCount[] | null;
    density: DensityModel | null;
}

export interface LeafMethod { classNode: Index; methodNode: Index }

export function collectLeaves(nodes: Indices): LeafMethod[] {
    const out: LeafMethod[] = [];
    const walk = (list: Indices) => {
        for (const n of list) {
            if (n.type === 'overview' || n.type === 'system-view') continue;
            if (n.testClass && n.children && n.children.every(c => c.testMethod)) {
                for (const c of n.children) out.push({classNode: n, methodNode: c});
            } else if (n.children) {
                walk(n.children);
            }
        }
    };
    walk(nodes);
    return out;
}

const union = (a?: string[], b?: string[]): string[] => [...new Set([...(a ?? []), ...(b ?? [])])];
export const effectiveTags = (l: LeafMethod) => union(l.classNode.tags, l.methodNode.tags);
export const effectiveEpics = (l: LeafMethod) => union(l.classNode.epics, l.methodNode.epics);
export const effectiveIssues = (l: LeafMethod) => union(l.classNode.issues, l.methodNode.issues);

const emptyCounts = (): StateCounts => ({passed: 0, failed: 0, disabled: 0, notExecuted: 0, total: 0});

function addState(c: StateCounts, state: TestState) {
    c.total++;
    switch (state) {
        case 'Passed': c.passed++; break;
        case 'Failed': c.failed++; break;
        case 'Disabled': c.disabled++; break;
        case 'Not Executed': c.notExecuted++; break;
        default: {
            const _exhaustive: never = state;
            return _exhaustive;
        }
    }
}

function packageKey(testClass: string, depth: number): string {
    const pkg = testClass.split('.').slice(0, -1);
    return (depth > 0 ? pkg.slice(0, depth) : pkg).join('.');
}

function groupBy(leaves: LeafMethod[], keysOf: (l: LeafMethod) => string[]): GroupCounts[] | null {
    const groups = new Map<string, GroupCounts>();
    for (const l of leaves) {
        for (const key of keysOf(l)) {
            const g = groups.get(key) ?? {key, ...emptyCounts()};
            addState(g, l.methodNode.state);
            groups.set(key, g);
        }
    }
    if (groups.size === 0) return null;
    return [...groups.values()].sort((a, b) => b.total - a.total || a.key.localeCompare(b.key));
}

function epicGroups(leaves: LeafMethod[]): EpicGroup[] | null {
    const groups = new Map<string, EpicGroup>();
    for (const l of leaves) {
        const issues = effectiveIssues(l);
        for (const epic of effectiveEpics(l)) {
            const g = groups.get(epic) ?? {epic, issues: [], epicOnly: 0, ...emptyCounts()};
            addState(g, l.methodNode.state);
            if (issues.length === 0) g.epicOnly++;
            for (const issue of issues) {
                let ic = g.issues.find(i => i.issue === issue);
                if (!ic) { ic = {issue, ...emptyCounts()}; g.issues.push(ic); }
                addState(ic, l.methodNode.state);
            }
            groups.set(epic, g);
        }
    }
    if (groups.size === 0) return null;
    return [...groups.values()]
        .map(g => ({...g, issues: [...g.issues].sort((a, b) => b.total - a.total || a.issue.localeCompare(b.issue))}))
        .sort((a, b) => b.total - a.total || a.epic.localeCompare(b.epic));
}

function failures(leaves: LeafMethod[], depth: number): FailuresModel | null {
    const failed = leaves.filter(l => l.methodNode.state === 'Failed');
    if (failed.length === 0) return null;
    const rows: FailureRow[] = failed.map(l => ({
        nodeId: l.methodNode.id,
        classId: l.classNode.id,
        testClass: l.classNode.testClass,
        testMethod: l.methodNode.testMethod ?? '',
        displayName: l.methodNode.displayName,
        epics: effectiveEpics(l),
        issues: effectiveIssues(l),
    }));
    const byPkg = new Map<string, number>();
    for (const l of failed) {
        const k = packageKey(l.classNode.testClass, depth);
        byPkg.set(k, (byPkg.get(k) ?? 0) + 1);
    }
    let concentration: {pkg: string; count: number} | null = null;
    if (failed.length > 1) {
        const [pkg, count] = [...byPkg.entries()].sort((a, b) => b[1] - a[1])[0];
        if (count * 2 > failed.length) concentration = {pkg, count};
    }
    return {rows, concentration};
}

const BUCKETS: {label: string; maxMs: number}[] = [
    {label: '<100ms', maxMs: 100},
    {label: '100ms–1s', maxMs: 1000},
    {label: '1–5s', maxMs: 5000},
    {label: '5–30s', maxMs: 30000},
    {label: '>30s', maxMs: Number.POSITIVE_INFINITY},
];

const elapsedOf = (l: LeafMethod): number | null =>
    l.methodNode.timing ? l.methodNode.timing.reduce((acc, [, e]) => acc + e, 0) : null;

function durations(leaves: LeafMethod[]): DurationBucket[] | null {
    const elapsed = leaves.map(elapsedOf).filter((e): e is number => e !== null);
    if (elapsed.length === 0) return null;
    return BUCKETS.map((bucket, i) => ({
        label: bucket.label,
        count: elapsed.filter(e => e < bucket.maxMs && (i === 0 || e >= BUCKETS[i - 1].maxMs)).length,
    }));
}

function slowest(leaves: LeafMethod[]): SlowRow[] | null {
    const rows = leaves.flatMap(l => {
        const e = elapsedOf(l);
        return e === null ? [] : [{nodeId: l.methodNode.id, classId: l.classNode.id, testClass: l.classNode.testClass, testMethod: l.methodNode.testMethod ?? '', displayName: l.methodNode.displayName, elapsedMs: e}];
    });
    if (rows.length === 0) return null;
    return rows.sort((a, b) => b.elapsedMs - a.elapsedMs).slice(0, 10);
}

function participants(leaves: LeafMethod[]): GroupCount[] | null {
    const counts = new Map<string, number>();
    for (const l of leaves) {
        for (const [name, n] of Object.entries(l.methodNode.participants ?? {})) counts.set(name, (counts.get(name) ?? 0) + n);
    }
    if (counts.size === 0) return null;
    return [...counts.entries()].map(([key, count]) => ({key, count})).sort((a, b) => b.count - a.count || a.key.localeCompare(b.key));
}

function density(leaves: LeafMethod[]): DensityModel | null {
    const hasAny = leaves.some(l => l.methodNode.assertions !== undefined || l.methodNode.expandables !== undefined || l.methodNode.timing !== undefined);
    if (!hasAny) return null;
    const assertions = leaves.reduce((acc, l) => acc + (l.methodNode.assertions ?? 0), 0);
    const withExpandables = leaves.filter(l => (l.methodNode.expandables ?? 0) > 0).length;
    const parameterised = leaves.filter(l => (l.methodNode.timing?.length ?? 0) > 1).length;

    const perClass = new Map<string, {classId: string; testClass: string; assertions: number; tests: number}>();
    for (const l of leaves) {
        const c = perClass.get(l.classNode.id) ?? {classId: l.classNode.id, testClass: l.classNode.testClass, assertions: 0, tests: 0};
        c.assertions += l.methodNode.assertions ?? 0;
        c.tests++;
        perClass.set(l.classNode.id, c);
    }
    const weakestEntry = [...perClass.values()].map(c => ({classId: c.classId, testClass: c.testClass, assertionsPerTest: c.assertions / c.tests}))
        .sort((a, b) => a.assertionsPerTest - b.assertionsPerTest || a.testClass.localeCompare(b.testClass))[0] ?? null;

    return {assertionsPerTest: assertions / leaves.length, expandableShare: withExpandables / leaves.length, parameterised, weakest: weakestEntry};
}

export function packageDepthFor(display: string, root?: string): number {
    return display === 'HideCommonPackages' && root ? root.split('.').length + 1 : 0;
}

export function buildOverview(filtered: Indices, packageDepth: number): OverviewModel {
    const leaves = collectLeaves(filtered);
    if (leaves.length === 0) {
        return {results: null, byTag: null, byPackage: null, byEpic: null, failures: null, durations: null, slowest: null, participants: null, density: null};
    }
    const results = emptyCounts();
    for (const l of leaves) addState(results, l.methodNode.state);
    return {
        results,
        byTag: groupBy(leaves, effectiveTags),
        byPackage: groupBy(leaves, l => [packageKey(l.classNode.testClass, packageDepth)]),
        byEpic: epicGroups(leaves),
        failures: failures(leaves, packageDepth),
        durations: durations(leaves),
        slowest: slowest(leaves),
        participants: participants(leaves),
        density: density(leaves),
    };
}
