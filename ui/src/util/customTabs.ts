import {Invocation} from '@/types/Test';

export interface CustomTab {
    id: string;
    label: string;
    file?: string;
    mediaType: string;
    empty: boolean;
    kind: 'custom';
}

export function customTabsOf(invocation: Invocation): CustomTab[] {
    return (invocation.customTabContents ?? [])
        .filter(t => t.file !== undefined || t.entries !== undefined)
        .map(t => ({
            id: t.tabId,
            label: t.label,
            file: t.file,
            mediaType: t.mediaType ?? 'text/plain',
            empty: t.file === undefined && t.entries === 0,
            kind: 'custom' as const,
        }));
}
