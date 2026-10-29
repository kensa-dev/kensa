import {Token} from '@/types/Test';

export const keywordColors: Record<string, string> = {
    'given': 'text-sky-600 dark:text-sky-400',
    'when':  'text-amber-600 dark:text-amber-400',
    'then':  'text-emerald-600 dark:text-emerald-500',
};

const MAJOR_KEYWORDS = new Set(['given', 'when', 'then']);

export const isMajorKeyword = (v: string) => MAJOR_KEYWORDS.has(v.toLowerCase().trim());

export const keywordClass = (value: string, inheritedKeyword?: string): string => {
    const key = value.toLowerCase().trim();
    return keywordColors[key] ?? (inheritedKeyword ? (keywordColors[inheritedKeyword] ?? 'text-foreground') : 'text-foreground');
};

export const inheritedKeywords = (tokens: Token[], inheritedKeyword: string | undefined): (string | undefined)[] => {
    let current = inheritedKeyword;
    return tokens.map(token => {
        if (!token.types?.includes('tk-kw')) return undefined;
        if (isMajorKeyword(token.value)) current = token.value.toLowerCase().trim();
        return current;
    });
};
