import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { Indices } from '@/types/Index';
import {
    collapseAll as collapseAllPure,
    EMPTY,
    expandAll as expandAllPure,
    ExpansionState,
    isCollapsed as isCollapsedPure,
    setCollapsed as setCollapsedPure,
} from '@/state/treeExpansion';
import { loadExpansion, saveExpansion } from '@/state/treeExpansionStorage';

export interface TreeExpansionApi {
    isCollapsed: (id: string) => boolean;
    setCollapsed: (id: string, collapsed: boolean) => void;
    expandAll: () => void;
    collapseAll: () => void;
}

const TreeExpansionContext = createContext<TreeExpansionApi | null>(null);

export interface TreeExpansionProviderProps {
    nodes: Indices;
    // When true, all folders read as expanded — used while a search filter is
    // active so matches inside user-collapsed folders are visible. The
    // persisted state is left untouched so the user's prior shape is restored
    // once the filter clears.
    searchActive?: boolean;
    children: React.ReactNode;
}

export const TreeExpansionProvider: React.FC<TreeExpansionProviderProps> = ({ nodes, searchActive = false, children }) => {
    const [state, setState] = useState<ExpansionState>(() => loadExpansion());
    const nodesRef = useRef(nodes);
    nodesRef.current = nodes;

    useEffect(() => {
        saveExpansion(state);
    }, [state]);

    const api = useMemo<TreeExpansionApi>(() => ({
        isCollapsed: (id) => isCollapsedPure(state, id, { searchActive }),
        setCollapsed: (id, collapsed) => setState((s) => setCollapsedPure(s, id, collapsed)),
        expandAll: () => setState((s) => expandAllPure(s, nodesRef.current)),
        collapseAll: () => setState((s) => collapseAllPure(s, nodesRef.current)),
    }), [state, searchActive]);

    return React.createElement(TreeExpansionContext.Provider, { value: api }, children);
};

export function useTreeExpansion(): TreeExpansionApi {
    const ctx = useContext(TreeExpansionContext);
    if (!ctx) {
        return {
            isCollapsed: () => false,
            setCollapsed: () => {},
            expandAll: () => {},
            collapseAll: () => {},
        };
    }
    return ctx;
}

export type { ExpansionState };
export { EMPTY };
