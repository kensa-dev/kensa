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
