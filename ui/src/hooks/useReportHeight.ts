import {useEffect} from 'react';
import {nextHeightPost} from '@/util/embedHeight';
import {isEmbedded} from '@/util/embedded';

// Inside a frame, post the document height to the host on every resize so
// kensa-embed.js can size the iframe. The payload is a bare number, so the
// message goes to any origin. Measurements are folded into one post per frame.
export function useReportHeight(): void {
    useEffect(() => {
        if (!isEmbedded() || typeof ResizeObserver === 'undefined') return;
        let posted: number | null = null;
        let frame = 0;
        const post = () => {
            frame = 0;
            const message = nextHeightPost(posted, document.documentElement.scrollHeight);
            if (!message) return;
            posted = message.height;
            window.parent.postMessage(message, '*');
        };
        const observer = new ResizeObserver(() => {
            if (frame === 0) frame = requestAnimationFrame(post);
        });
        observer.observe(document.documentElement);
        post();
        return () => {
            observer.disconnect();
            if (frame !== 0) cancelAnimationFrame(frame);
        };
    }, []);
}
