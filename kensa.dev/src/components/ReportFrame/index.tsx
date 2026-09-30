import type { ReactNode } from 'react';
import { useEffect, useRef, useState } from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

// A report embed with a picture of itself as the first frame. The picture is what the
// server renders and what a narrow frame keeps: squeezed, the live report scrolls its
// diagram and wraps its sentences until the card is twice the picture's height. The
// test is the frame's own width, not the viewport's, because the hero's column is narrow
// on a laptop screen. It is the 760px the pictures were captured at, less the 16px the
// frame bleeds. From there the live embed mounts behind the picture and replaces it
// once it has rendered and posted its height.
const LIVE_FROM_PX = 744;
const HEIGHT_MESSAGE = 'kensa:height';
// The embed posts once for its loading skeleton before the test has rendered; a real
// card is several hundred pixels, so anything shorter keeps the picture in place.
const RENDERED_FROM_PX = 200;

interface ReportFrameProps {
    embedSrc: string;
    fullUrl: string;
    picture: string;
    alt: string;
    title: string;
}

export default function ReportFrame({ embedSrc, fullUrl, picture, alt, title }: ReportFrameProps): ReactNode {
    const [live, setLive] = useState(false);
    const [rendered, setRendered] = useState(false);
    const container = useRef<HTMLDivElement>(null);
    const frame = useRef<HTMLIFrameElement>(null);
    const pictureUrl = useBaseUrl(picture);

    useEffect(() => {
        const element = container.current;
        if (!element) return;
        const observer = new ResizeObserver(([entry]) => {
            const wide = entry.contentRect.width >= LIVE_FROM_PX;
            setLive(wide);
            if (!wide) setRendered(false);
        });
        observer.observe(element);
        return () => observer.disconnect();
    }, []);

    useEffect(() => {
        if (!live) return;
        const onMessage = (event: MessageEvent) => {
            const data = event.data as { type?: unknown; height?: unknown } | null;
            const isHeight = typeof data === 'object' && data !== null && data.type === HEIGHT_MESSAGE;
            const tall = isHeight && typeof data.height === 'number' && data.height >= RENDERED_FROM_PX;
            if (tall && frame.current && event.source === frame.current.contentWindow) setRendered(true);
        };
        window.addEventListener('message', onMessage);
        return () => window.removeEventListener('message', onMessage);
    }, [live]);

    const showPicture = !live || !rendered;

    return (
        <div ref={container} className={styles.frame}>
            {showPicture && (
                <a className={styles.pictureLink} href={fullUrl} target="_blank" rel="noopener noreferrer">
                    <img className={styles.picture} src={pictureUrl} alt={alt} width={760} />
                </a>
            )}
            {live && (
                <iframe
                    ref={frame}
                    className={rendered ? styles.embed : styles.embedPending}
                    data-kensa-embed
                    src={embedSrc}
                    title={title}
                    referrerPolicy="no-referrer"
                />
            )}
        </div>
    );
}
