import {type ClassValue, clsx} from "clsx"
import {twMerge} from "tailwind-merge"
import {useNavigate, useLocation} from 'react-router-dom';
import type {NavigateOptions, To} from 'react-router-dom';

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

export const isNative = (): boolean => {
    return '__TAURI_INTERNALS__' in window;
};

export async function loadJson<T>(
    path: string,
    label: string,
    options: { baseUrl?: string; signal?: AbortSignal } = {}
): Promise<T | null> {
    try {
        const normalizedPath = path.replace(/^\.\//, '');
        const url = `${options.baseUrl || '.'}/${normalizedPath}`;

        const res = await fetch(url, {signal: options.signal});

        if (!res.ok) {
            console.warn(`${label} load failed: ${res.status} ${res.statusText} (${url})`);
            return null;
        }

        return (await res.json()) as T;
    } catch (err) {
        if (err instanceof DOMException && err.name === "AbortError") return null;

        console.error(`Error loading ${label} from ${path}:`, err);
        return null;
    }
}

export async function loadText(
    path: string,
    label: string,
    options: { baseUrl?: string; signal?: AbortSignal } = {}
): Promise<string | null> {
    try {
        const normalizedPath = path.replace(/^\.\//, '');
        const url = `${options.baseUrl || '.'}/${normalizedPath}`;

        const res = await fetch(url, {signal: options.signal});

        if (!res.ok) {
            console.warn(`${label} load failed: ${res.status} ${res.statusText} (${url})`);
            return null;
        }

        return await res.text();
    } catch (err) {
        // Abort is expected when switching tabs quickly.
        if (err instanceof DOMException && err.name === "AbortError") return null;

        console.error(`Error loading ${label} from ${path}:`, err);
        return null;
    }
}

export function useNavigateWithSearch() {
    const navigate = useNavigate();
    const location = useLocation();

    return (to: To, options: NavigateOptions = {}) => {
        if (typeof to === 'string') {
            const [pathname, currentSearch] = to.split('?');
            const preservedSearch = currentSearch
                ? `?${currentSearch}`
                : location.search;

            return navigate(`${pathname}${preservedSearch}`, options);
        }

        return navigate(to, options);
    };
}

export function getAllTextNodes(root: Node): Text[] {
    const textNodes: Text[] = [];
    const stack = [root];
    while (stack.length) {
        const node = stack.pop();
        if (node?.nodeType === Node.TEXT_NODE) {
            textNodes.push(node as Text);
        } else if (node?.childNodes) {
            for (let i = node.childNodes.length - 1; i >= 0; i--) {
                stack.push(node.childNodes[i]);
            }
        }
    }
    return textNodes;
}

export function buildHighlightRegex(highlights: string[]): RegExp {
    const escaped = highlights.map(h => h.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&'));
    return new RegExp(`(${escaped.join('|')})`, 'g');
}

export function removeHighlightSpans(root: HTMLElement, cls: string) {
    root.querySelectorAll(`.${cls}`).forEach(mark => {
        const parent = mark.parentNode;
        if (parent) {
            while (mark.firstChild) parent.insertBefore(mark.firstChild, mark);
            parent.removeChild(mark);
            parent.normalize();
        }
    });
}

export function applyKensaHighlights(root: HTMLElement, highlights: string[]) {
    if (highlights.length === 0) return;

    const regExp = buildHighlightRegex(highlights);

    getAllTextNodes(root).forEach(node => {
        const text = node.textContent ?? '';
        if (!regExp.test(text)) return;
        regExp.lastIndex = 0;

        const parent = node.parentNode;
        if (!parent) return;

        const frag = document.createDocumentFragment();
        let lastIndex = 0;
        let result: RegExpExecArray | null;
        while ((result = regExp.exec(text)) !== null) {
            if (result.index > lastIndex) {
                frag.appendChild(document.createTextNode(text.slice(lastIndex, result.index)));
            }
            const mark = document.createElement('span');
            mark.className = 'kensa-highlight';
            mark.textContent = result[0];
            frag.appendChild(mark);
            lastIndex = result.index + result[0].length;
        }
        if (lastIndex < text.length) {
            frag.appendChild(document.createTextNode(text.slice(lastIndex)));
        }
        parent.replaceChild(frag, node);
    });
}
