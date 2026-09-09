import {Indices} from '@/types/Index';
import {filterIndices} from '@/utils/filterIndices';
import {parseQuery} from '@/util/queryMeta';

export interface FilterRoute {
    query: string;
}

export interface FilterRouteTarget {
    testId: string;
    method: string | null;
}

const ROUTE_PATTERN = /^\/(issue|epic)\/([^/]+)$/;

// A Jira link points at `#/issue/<key>` or `#/epic/<id>`; it carries no test
// selection of its own, just the key to resolve against the loaded indices.
export function parseFilterRoute(pathname: string): FilterRoute | null {
    const match = pathname.match(ROUTE_PATTERN);
    if (!match) return null;
    const [, kind, key] = match;
    let decodedKey = key;
    try {
        decodedKey = decodeURIComponent(key);
    } catch {
        decodedKey = key;
    }
    return {query: `${kind}:${decodedKey}`};
}

export function filterRouteTarget(indices: Indices, route: FilterRoute): FilterRouteTarget | null {
    const {firstMatchingTest, firstMatchingMethod} = filterIndices(indices, parseQuery(route.query), '');
    if (!firstMatchingTest) return null;
    return {testId: firstMatchingTest.id, method: firstMatchingMethod};
}
