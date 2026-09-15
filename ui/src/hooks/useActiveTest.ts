import {RefObject, useEffect, useMemo, useState} from 'react';
import {activeIndex, applyEntries, Crossing, entryToCrossing} from '@/util/activeTest';

// Watches the card headers in `targets` against the top edge of `root` (the
// report's scroll pane) and returns the index of the active test: the last
// expanded card whose header has scrolled above that edge, or -1 when none.
// Thresholds 0 and 1 together catch the header's top edge crossing the line
// in both directions; a threshold of 0 alone fires only once it is fully out.
// Inert unless `enabled`; re-observes whenever `count` changes. `expanded`
// only re-derives the result; it does not rebuild the observer.
export function useActiveTest(
    targets: RefObject<(Element | null)[]>,
    root: RefObject<Element | null> | undefined,
    enabled: boolean,
    count: number,
    expanded: ReadonlySet<number>,
): number {
    const [crossed, setCrossed] = useState<ReadonlySet<number>>(() => new Set());

    useEffect(() => {
        if (!enabled || typeof IntersectionObserver === 'undefined') return;
        const elements = targets.current ?? [];
        const indexOf = (target: Element) => elements.indexOf(target);
        const observer = new IntersectionObserver((entries) => {
            const crossings = entries
                .map(entry => entryToCrossing(entry, indexOf))
                .filter((c): c is Crossing => c !== null);
            setCrossed(prev => applyEntries(prev, crossings));
        }, {root: root?.current ?? null, threshold: [0, 1]});
        for (const element of elements.slice(0, count)) {
            if (element) observer.observe(element);
        }
        return () => {
            observer.disconnect();
            setCrossed(new Set());
        };
    }, [targets, root, enabled, count]);

    return useMemo(() => activeIndex(crossed, expanded), [crossed, expanded]);
}
