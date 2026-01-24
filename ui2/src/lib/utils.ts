import {type ClassValue, clsx} from "clsx"
import {twMerge} from "tailwind-merge"
import { useNavigate, useLocation } from 'react-router-dom';
import type { NavigateOptions, To } from 'react-router-dom';

export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs))
}

export const isNative = (): boolean => {
    return '__TAURI_INTERNALS__' in window;
};

export async function loadJson<T>(
    path: string,
    label: string,
    options: { baseUrl?: string } = {}
): Promise<T | null> {
    try {
        const normalizedPath = path.replace(/^\.?\//, '');
        const url = `${options.baseUrl || '.'}/${normalizedPath}`;

        const res = await fetch(url);

        if (!res.ok) {
            console.warn(`${label} load failed: ${res.status} ${res.statusText} (${url})`);
            return null;
        }

        return (await res.json()) as T;
    } catch (err) {
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