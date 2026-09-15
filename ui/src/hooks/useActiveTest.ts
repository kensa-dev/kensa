import {RefObject, useEffect, useState} from 'react';
import {activeIndex, applyEntries, Crossing, entryToCrossing} from '@/util/activeTest';

// Watches the card headers in `targets` against the top edge of `root` (the
// report's scroll pane) and returns the index of the active test: the last
// card whose header has scrolled above that edge, or -1 at the top of the page.
// Thresholds 0 and 1 together catch the header's top edge crossing the line
// in both directions; a threshold of 0 alone fires only once it is fully out.
// Inert unless `enabled`; re-observes whenever `count` changes.
export function useActiveTest(
    targets: RefObject<(Element | null)[]>,
    root: RefObject<Element | null> | undefined,
    enabled: boolean,
    count: number,
): number {
    const [active, setActive] = useState(-1);

    useEffect(() => {
        if (!enabled || typeof IntersectionObserver === 'undefined') return;
        const elements = targets.current ?? [];
        const indexOf = (target: Element) => elements.indexOf(target);
        let crossed = new Set<number>();
        const observer = new IntersectionObserver((entries) => {
            const crossings = entries
                .map(entry => entryToCrossing(entry, indexOf))
                .filter((c): c is Crossing => c !== null);
            crossed = applyEntries(crossed, crossings);
            setActive(activeIndex(crossed));
        }, {root: root?.current ?? null, threshold: [0, 1]});
        for (const element of elements.slice(0, count)) {
            if (element) observer.observe(element);
        }
        return () => {
            observer.disconnect();
            setActive(-1);
        };
    }, [targets, root, enabled, count]);

    return active;
}
