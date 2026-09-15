export type FilterPrefix = 'tag' | 'issue' | 'epic';

const isToken = (prefix: FilterPrefix) => (part: string): boolean =>
    part.startsWith(`${prefix}:`) && part.length > prefix.length + 1;

const tokens = (query: string): string[] => query.split(/\s+/).filter(Boolean);

export const nextQueryAfterFilterClick = (currentQuery: string, prefix: FilterPrefix, value: string, additive: boolean): string => {
    const parts = tokens(currentQuery);
    const clickedToken = `${prefix}:${value}`;

    if (!additive) {
        const others = parts.filter(p => !isToken(prefix)(p));
        return [...others, clickedToken].join(' ');
    }

    const hasClicked = parts.includes(clickedToken);
    const next = hasClicked ? parts.filter(p => p !== clickedToken) : [...parts, clickedToken];
    return next.join(' ');
};

export const selectedFilterValues = (query: string, prefix: FilterPrefix): Set<string> =>
    new Set(tokens(query).filter(isToken(prefix)).map(p => p.slice(prefix.length + 1)));

export const nextQueryAfterTagClick = (currentQuery: string, clickedTag: string, additive: boolean): string =>
    nextQueryAfterFilterClick(currentQuery, 'tag', clickedTag, additive);

export const selectedTagsFromQuery = (query: string): Set<string> =>
    selectedFilterValues(query, 'tag');
