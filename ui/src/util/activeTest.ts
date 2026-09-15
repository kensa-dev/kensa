// The active test is the last expanded card whose header has scrolled under
// the report header. Observer callbacks report only the cards that crossed the
// line, so the set of crossed cards is kept and folded forward; the active one
// is the highest index both crossed and expanded, or -1 when there is none.

export type Crossing = {index: number; above: boolean};

export const applyEntries = (crossed: ReadonlySet<number>, crossings: Crossing[]): Set<number> => {
    const next = new Set(crossed);
    for (const {index, above} of crossings) {
        if (above) next.add(index);
        else next.delete(index);
    }
    return next;
};

// Same instance back when nothing changes, so a state setter can skip a render.
export const withExpanded = (expanded: ReadonlySet<number>, index: number, isExpanded: boolean): ReadonlySet<number> => {
    if (expanded.has(index) === isExpanded) return expanded;
    const next = new Set(expanded);
    if (isExpanded) next.add(index);
    else next.delete(index);
    return next;
};

export const activeIndex = (crossed: ReadonlySet<number>, expanded: ReadonlySet<number>): number => {
    let max = -1;
    for (const index of crossed) if (index > max && expanded.has(index)) max = index;
    return max;
};

// The shape of an IntersectionObserverEntry this needs, kept structural so the
// mapping runs without a DOM. `rootBounds` is null when the root is not an
// ancestor of the target; there is no line to compare against then.
export type ObservedEntry<T> = {
    target: T;
    boundingClientRect: {top: number};
    rootBounds: {top: number} | null;
};

export const entryToCrossing = <T>(entry: ObservedEntry<T>, indexOf: (target: T) => number): Crossing | null => {
    const index = indexOf(entry.target);
    if (index < 0 || entry.rootBounds === null) return null;
    return {index, above: entry.boundingClientRect.top < entry.rootBounds.top};
};
