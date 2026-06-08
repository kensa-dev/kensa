import { createContext, ReactNode, useCallback, useContext, useMemo, useState } from 'react';
import {
    findByFixtureName,
    findByValue,
    type MergedIndex,
    type SuiteSearchLocation,
} from '@/lib/suiteSearch';
import type { ActiveTerm } from '@/util/suiteSearchNav';

const EMPTY_INDEX: MergedIndex = { terms: [] };

export interface SuiteSearchResult extends SuiteSearchLocation {
    value: string;
}

interface SuiteSearchState {
    mergedIndex: MergedIndex;
    activeTerm: ActiveTerm | null;
    results: SuiteSearchResult[];
    isPanelOpen: boolean;
    highlightValue: string | null;
    searchValue: (value: string) => void;
    searchFixture: (name: string, highlightValue?: string) => void;
    openPanel: () => void;
    closePanel: () => void;
    clear: () => void;
}

const SuiteSearchContext = createContext<SuiteSearchState>({
    mergedIndex: EMPTY_INDEX,
    activeTerm: null,
    results: [],
    isPanelOpen: false,
    highlightValue: null,
    searchValue: () => undefined,
    searchFixture: () => undefined,
    openPanel: () => undefined,
    closePanel: () => undefined,
    clear: () => undefined,
});

export const useSuiteSearch = () => useContext(SuiteSearchContext);

interface SuiteSearchProviderProps {
    mergedIndex: MergedIndex;
    highlightValue: string | null;
    onHighlightValue?: (value: string | null) => void;
    children: ReactNode;
}

export const SuiteSearchProvider = ({ mergedIndex, highlightValue, onHighlightValue, children }: SuiteSearchProviderProps) => {
    const [activeTerm, setActiveTerm] = useState<ActiveTerm | null>(null);
    const [isPanelOpen, setIsPanelOpen] = useState(false);

    const searchValue = useCallback((value: string) => {
        setActiveTerm({ kind: 'value', query: value });
        setIsPanelOpen(true);
        onHighlightValue?.(value);
    }, [onHighlightValue]);

    const searchFixture = useCallback((name: string, highlightValue?: string) => {
        setActiveTerm({ kind: 'fixture', query: name });
        setIsPanelOpen(true);
        onHighlightValue?.(highlightValue ?? null);
    }, [onHighlightValue]);

    const openPanel = useCallback(() => setIsPanelOpen(true), []);
    const closePanel = useCallback(() => setIsPanelOpen(false), []);

    const clear = useCallback(() => {
        setActiveTerm(null);
        setIsPanelOpen(false);
        onHighlightValue?.(null);
    }, [onHighlightValue]);

    const results = useMemo<SuiteSearchResult[]>(() => {
        if (!activeTerm) return [];
        if (activeTerm.kind === 'value') {
            return findByValue(mergedIndex, activeTerm.query).map((location) => ({
                ...location,
                value: activeTerm.query,
            }));
        }
        return findByFixtureName(mergedIndex, activeTerm.query).flatMap((m) =>
            m.locations.map((location) => ({ ...location, value: m.value })),
        );
    }, [activeTerm, mergedIndex]);

    const value = useMemo<SuiteSearchState>(
        () => ({
            mergedIndex,
            activeTerm,
            results,
            isPanelOpen,
            highlightValue,
            searchValue,
            searchFixture,
            openPanel,
            closePanel,
            clear,
        }),
        [mergedIndex, activeTerm, results, isPanelOpen, highlightValue, searchValue, searchFixture, openPanel, closePanel, clear],
    );

    return <SuiteSearchContext.Provider value={value}>{children}</SuiteSearchContext.Provider>;
};
