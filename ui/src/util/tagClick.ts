const isTagToken = (part: string): boolean => part.startsWith('tag:') && part.length > 4;

const tokens = (query: string): string[] => query.split(/\s+/).filter(Boolean);

export const nextQueryAfterTagClick = (currentQuery: string, clickedTag: string, additive: boolean): string => {
    const parts = tokens(currentQuery);
    const clickedToken = `tag:${clickedTag}`;

    if (!additive) {
        const nonTag = parts.filter(p => !isTagToken(p));
        return [...nonTag, clickedToken].join(' ');
    }

    const hasClicked = parts.includes(clickedToken);
    const next = hasClicked ? parts.filter(p => p !== clickedToken) : [...parts, clickedToken];
    return next.join(' ');
};

export const selectedTagsFromQuery = (query: string): Set<string> =>
    new Set(tokens(query).filter(isTagToken).map(p => p.slice(4)));
