import { useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import styles from './styles.module.css';

function Lightbox({ src, alt, onClose }: { src: string; alt: string; onClose: () => void }): ReactNode {
    useEffect(() => {
        const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [onClose]);

    return (
        <div className={styles.lightboxOverlay} onClick={onClose} role="dialog" aria-modal="true" aria-label={alt}>
            <button className={styles.lightboxClose} onClick={onClose} aria-label="Close">✕</button>
            <img
                src={src}
                alt={alt}
                className={styles.lightboxImage}
                onClick={(e) => e.stopPropagation()}
            />
        </div>
    );
}

export default function HomepageShowcase(): ReactNode {
    const [lightbox, setLightbox] = useState<{ src: string; alt: string } | null>(null);

    return (
        <section className={styles.showcase}>
            <div className="container">
                <p className={styles.eyebrow}>See it in action</p>
                <div className={styles.columns}>
                    <div className={styles.column}>
                        <p className={styles.label}>Write this&hellip;</p>
                        <button
                            className={styles.screenshotBtn}
                            onClick={() => setLightbox({ src: '/img/code-example.png', alt: 'Kensa test written in Kotlin' })}
                            aria-label="View full-size code example"
                        >
                            <img
                                src="/img/code-example.png"
                                alt="Kensa test written in Kotlin"
                                className={styles.screenshot}
                            />
                        </button>
                    </div>
                    <div className={styles.arrow} aria-hidden="true">→</div>
                    <div className={styles.column}>
                        <p className={styles.label}>&hellip;get this</p>
                        <button
                            className={styles.screenshotBtn}
                            onClick={() => setLightbox({ src: '/img/report-example.png', alt: 'Kensa generated HTML report with sequence diagram' })}
                            aria-label="View full-size report example"
                        >
                            <img
                                src="/img/report-example.png"
                                alt="Kensa generated HTML report with sequence diagram"
                                className={styles.screenshot}
                            />
                        </button>
                    </div>
                </div>
            </div>
            {lightbox && <Lightbox src={lightbox.src} alt={lightbox.alt} onClose={() => setLightbox(null)} />}
        </section>
    );
}
