export const HEIGHT_MESSAGE_TYPE = 'kensa:height';

export interface HeightMessage {
    type: typeof HEIGHT_MESSAGE_TYPE;
    height: number;
}

export function heightMessage(height: number): HeightMessage {
    return {type: HEIGHT_MESSAGE_TYPE, height};
}

export function isHeightMessage(data: unknown): data is HeightMessage {
    if (typeof data !== 'object' || data === null) return false;
    const {type, height} = data as {type?: unknown; height?: unknown};
    return type === HEIGHT_MESSAGE_TYPE && typeof height === 'number' && Number.isFinite(height) && height >= 0;
}

// The height to post after a resize, or null when the rounded value has not moved.
export function nextHeightPost(previous: number | null, measured: number): HeightMessage | null {
    const height = Math.ceil(measured);
    return height === previous ? null : heightMessage(height);
}

// Something with a rendered height: the embed's content root, not the document,
// whose scrollHeight inside a frame never drops below the frame's own height.
export interface Measurable {
    getBoundingClientRect(): {height: number};
}

// Posts the target's height whenever it has changed since the last post, so a
// frame sized by the host follows the content when it shrinks as well as grows.
export function heightReporter(target: Measurable, post: (message: HeightMessage) => void): () => void {
    let posted: number | null = null;
    return () => {
        const message = nextHeightPost(posted, target.getBoundingClientRect().height);
        if (!message) return;
        posted = message.height;
        post(message);
    };
}
