import {isHeightMessage} from '@/util/embedHeight';

// The host page side of embed auto-height. Each embedded report posts
// {type: 'kensa:height', height} to its parent on resize; the frame is picked
// out by matching event.source against contentWindow, so no ids are needed and
// any number of embeds can share one page.

export interface EmbedFrame {
    contentWindow: unknown;
    style: {height: string};
}

export interface EmbedMessage {
    source: unknown;
    data: unknown;
}

export function handleMessage(frames: Iterable<EmbedFrame>, event: EmbedMessage): void {
    if (!isHeightMessage(event.data)) return;
    for (const frame of frames) {
        if (frame.contentWindow !== null && frame.contentWindow === event.source) {
            frame.style.height = `${event.data.height}px`;
            return;
        }
    }
}

export const EMBED_SELECTOR = 'iframe[data-kensa-embed]';

export function attachKensaEmbeds(root: ParentNode = document): () => void {
    const listener = (event: MessageEvent) =>
        handleMessage(root.querySelectorAll<HTMLIFrameElement>(EMBED_SELECTOR), event);
    window.addEventListener('message', listener);
    return () => window.removeEventListener('message', listener);
}
