import type { ReactNode } from 'react';
import { useEffect, useRef, useState } from 'react';
import useBaseUrl from '@docusaurus/useBaseUrl';
import styles from './styles.module.css';

// A report embed with a picture of itself as the first frame. The picture is what the
// server renders and what a phone keeps: a report is not a phone-sized document, and a
// live frame there costs two cross-origin documents and jitters as the browser bar
// hides and shows. From 768px up (iPad portrait and wider) the live embed mounts
// behind the picture and replaces it once it has rendered and posted its height.
const LIVE_FROM = '(min-width: 768px)';
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
    const frame = useRef<HTMLIFrameElement>(null);
    const pictureUrl = useBaseUrl(picture);

    useEffect(() => {
        const query = window.matchMedia(LIVE_FROM);
        const apply = () => {
            setLive(query.matches);
            if (!query.matches) setRendered(false);
        };
        apply();
        query.addEventListener('change', apply);
        return () => query.removeEventListener('change', apply);
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
        <div className={styles.frame}>
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
