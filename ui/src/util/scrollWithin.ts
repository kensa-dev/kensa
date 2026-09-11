// Scroll an element into view without leaving the document.
//
// `Element.scrollIntoView` walks every scrollable ancestor, and when the report is
// embedded in a cross-origin iframe Firefox carries that walk into the host page, so
// a report loading inside kensa.dev scrolls kensa.dev. Scrolling the nearest scroll
// container directly (or the window, which is bounded by the frame) cannot escape.

export type ScrollBlock = 'start' | 'nearest';

export const findScrollContainer = (el: Element): HTMLElement | null => {
    let node: HTMLElement | null = el.parentElement;
    while (node && node !== document.body) {
        const {overflowY} = getComputedStyle(node);
        const scrolls = overflowY === 'auto' || overflowY === 'scroll';
        if (scrolls && node.scrollHeight > node.clientHeight) return node;
        node = node.parentElement;
    }
    return null;
};

export const scrollWithin = (
    el: Element,
    {block = 'start', behavior = 'smooth'}: {block?: ScrollBlock; behavior?: ScrollBehavior} = {},
): void => {
    const container = findScrollContainer(el);
    const rect = el.getBoundingClientRect();

    if (container) {
        const containerRect = container.getBoundingClientRect();
        const offsetTop = rect.top - containerRect.top + container.scrollTop;
        if (block === 'start') {
            container.scrollTo({top: offsetTop, behavior});
            return;
        }
        const visibleTop = container.scrollTop;
        const visibleBottom = visibleTop + container.clientHeight;
        if (offsetTop < visibleTop) {
            container.scrollTo({top: offsetTop, behavior});
        } else if (offsetTop + rect.height > visibleBottom) {
            container.scrollTo({top: offsetTop + rect.height - container.clientHeight, behavior});
        }
        return;
    }

    const pageTop = rect.top + window.scrollY;
    if (block === 'start') {
        window.scrollTo({top: pageTop, behavior});
        return;
    }
    const viewTop = window.scrollY;
    const viewBottom = viewTop + window.innerHeight;
    if (pageTop < viewTop) {
        window.scrollTo({top: pageTop, behavior});
    } else if (pageTop + rect.height > viewBottom) {
        window.scrollTo({top: pageTop + rect.height - window.innerHeight, behavior});
    }
};
