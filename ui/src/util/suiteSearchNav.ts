import type { SuiteSearchLocation } from '@/lib/suiteSearch';

export type ActiveTermKind = 'value' | 'fixture';

export interface ActiveTerm {
    kind: ActiveTermKind;
    query: string;
}

export function nodeIdForLocation(location: SuiteSearchLocation): string {
    return `${location.sourceId}::${location.testId}`;
}

export function testKeyForLocation(location: SuiteSearchLocation): string {
    return `${location.sourceId}::${location.testId}::${location.testMethod}::${location.invocation}`;
}

export function highlightTermFor(activeTerm: ActiveTerm | null): string | null {
    if (!activeTerm) return null;
    return activeTerm.kind === 'value' ? activeTerm.query : null;
}
