import type { Indices } from '@/types/Index';

export interface ExpansionState {
    readonly collapsed: ReadonlySet<string>;
}

function isFolder(type: string | undefined): boolean {
    return type === 'project' || type === 'package';
}

export const EMPTY: ExpansionState = { collapsed: new Set<string>() };

export interface CollapsedReadOptions {
    // When true (typically while a search filter is active) folders are reported
    // as expanded regardless of the persisted collapsed set, so matches inside
    // user-collapsed folders are visible. The set itself is untouched.
    readonly searchActive?: boolean;
}

export function isCollapsed(state: ExpansionState, id: string, opts?: CollapsedReadOptions): boolean {
    if (opts?.searchActive) return false;
    return state.collapsed.has(id);
}

export function setCollapsed(state: ExpansionState, id: string, collapsed: boolean): ExpansionState {
    const next = new Set(state.collapsed);
    if (collapsed) {
        next.add(id);
    } else {
        next.delete(id);
    }
    return { collapsed: next };
}

export function collectFolderIds(nodes: Indices): string[] {
    const seen = new Set<string>();
    const out: string[] = [];
    const walk = (ns: Indices): void => {
        for (const n of ns) {
            if (isFolder(n.type)) {
                if (!seen.has(n.id)) {
                    seen.add(n.id);
                    out.push(n.id);
                }
                if (n.children) walk(n.children);
            }
        }
    };
    walk(nodes);
    return out;
}

export function expandAll(state: ExpansionState, nodes: Indices): ExpansionState {
    const ids = new Set(collectFolderIds(nodes));
    const next = new Set<string>();
    for (const id of state.collapsed) {
        if (!ids.has(id)) next.add(id);
    }
    return { collapsed: next };
}

export function collapseAll(state: ExpansionState, nodes: Indices): ExpansionState {
    const next = new Set(state.collapsed);
    for (const id of collectFolderIds(nodes)) next.add(id);
    return { collapsed: next };
}

export interface PersistedExpansion {
    version: 1;
    collapsed: string[];
}

// Hard cap on persisted set size to bound localStorage growth. The tail wins
// because new IDs are appended (Set preserves insertion order), so the cap
// drops the oldest entries.
export const MAX_PERSISTED = 5000;

export function toPersisted(state: ExpansionState): PersistedExpansion {
    return { version: 1, collapsed: [...state.collapsed].sort() };
}

export function fromPersisted(value: unknown): ExpansionState {
    if (!value || typeof value !== 'object') return EMPTY;
    const v = value as { version?: unknown; collapsed?: unknown };
    if (v.version !== 1) return EMPTY;
    if (!Array.isArray(v.collapsed)) return EMPTY;
    const source = v.collapsed.length > MAX_PERSISTED
        ? v.collapsed.slice(-MAX_PERSISTED)
        : v.collapsed;
    const next = new Set<string>();
    for (const id of source) {
        if (typeof id === 'string') next.add(id);
    }
    return { collapsed: next };
}
