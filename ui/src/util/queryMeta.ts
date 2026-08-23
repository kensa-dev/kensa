export interface QueryMeta {
    text: string;
    issues: string[];
    epics: string[];
    states: string[];
    tags: string[];
    packages: string[];
}

const PREFIXES = ['issue:', 'epic:', 'state:', 'tag:', 'pkg:'] as const;

export function isFilterTerm(part: string): boolean {
    return PREFIXES.some(p => part.startsWith(p) && part.length > p.length);
}

const valuesFor = (parts: string[], prefix: string): string[] =>
    parts.filter(p => p.startsWith(prefix) && p.length > prefix.length).map(p => p.slice(prefix.length));

export function parseQuery(query: string): QueryMeta {
    const parts = query.split(/\s+/).filter(Boolean);
    return {
        text: parts.filter(p => !isFilterTerm(p)).join(' '),
        issues: valuesFor(parts, 'issue:'),
        epics: valuesFor(parts, 'epic:'),
        states: valuesFor(parts, 'state:').map(s => s.toLowerCase()),
        tags: valuesFor(parts, 'tag:'),
        packages: valuesFor(parts, 'pkg:'),
    };
}

export function appendTerm(query: string, term: string): string {
    const parts = query.split(/\s+/).filter(Boolean);
    if (parts.includes(term)) return parts.join(' ');
    return [...parts, term].join(' ');
}
