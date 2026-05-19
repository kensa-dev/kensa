import type { Indices } from '@/types/Index';

export interface ExpansionState {
    readonly collapsed: ReadonlySet<string>;
}

function isFolder(type: string | undefined): boolean {
    return type === 'project' || type === 'package';
}

export const EMPTY: ExpansionState = { collapsed: new Set<string>() };

export function isCollapsed(state: ExpansionState, id: string): boolean {
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

export function toPersisted(state: ExpansionState): PersistedExpansion {
    return { version: 1, collapsed: [...state.collapsed].sort() };
}

export function fromPersisted(value: unknown): ExpansionState {
    if (!value || typeof value !== 'object') return EMPTY;
    const v = value as { version?: unknown; collapsed?: unknown };
    if (v.version !== 1) return EMPTY;
    if (!Array.isArray(v.collapsed)) return EMPTY;
    const next = new Set<string>();
    for (const id of v.collapsed) {
        if (typeof id === 'string') next.add(id);
    }
    return { collapsed: next };
}
