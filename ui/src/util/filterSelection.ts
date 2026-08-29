import {Index} from '@/types/Index';

export interface FilterSelection {
    testId: string | null;
    method: string;
}

// A link may name both a test and a filter (`/test/<id>?q=issue:X`). When the
// named test satisfies the filter it stays selected; the first match is only a
// fallback for when the path names nothing or names a test the filter excludes.
export function resolveFilterSelection(
    pathTestId: string | null,
    firstTest: Index | null,
    firstMethod: string | null,
    matchingMethodsMap: Map<string, string[]>,
): FilterSelection {
    if (pathTestId && matchingMethodsMap.has(pathTestId)) {
        return {testId: pathTestId, method: matchingMethodsMap.get(pathTestId)?.[0] ?? ''};
    }
    if (firstTest?.id) {
        return {testId: firstTest.id, method: firstMethod ?? ''};
    }
    return {testId: null, method: ''};
}
