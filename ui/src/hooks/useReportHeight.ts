import {RefObject, useEffect} from 'react';
import {heightReporter} from '@/util/embedHeight';
import {isEmbedded} from '@/util/embedded';

// Inside a frame, post the content root's height to the host on every resize so
// kensa-embed.js can size the iframe. The root is measured rather than the
// document because a document's height inside a frame never drops below the
// frame's own, so the frame would grow but never shrink. The payload is a bare
// number, so the message goes to any origin. Measurements are folded into one
// post per frame.
export function useReportHeight(root: RefObject<HTMLElement | null>): void {
    useEffect(() => {
        const element = root.current;
        if (!element || !isEmbedded() || typeof ResizeObserver === 'undefined') return;
        let frame = 0;
        const report = heightReporter(element, (message) => window.parent.postMessage(message, '*'));
        const post = () => {
            frame = 0;
            report();
        };
        const observer = new ResizeObserver(() => {
            if (frame === 0) frame = requestAnimationFrame(post);
        });
        observer.observe(element);
        post();
        return () => {
            observer.disconnect();
            if (frame !== 0) cancelAnimationFrame(frame);
        };
    }, [root]);
}
