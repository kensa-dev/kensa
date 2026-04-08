import { useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import styles from './styles.module.css';

interface Props {
    src: string;
    alt: string;
}

export default function BlogImage({ src, alt }: Props): ReactNode {
    const [open, setOpen] = useState(false);

    useEffect(() => {
        if (!open) return;
        const handler = (e: KeyboardEvent) => { if (e.key === 'Escape') setOpen(false); };
        window.addEventListener('keydown', handler);
        return () => window.removeEventListener('keydown', handler);
    }, [open]);

    return (
        <div className={styles.wrapper}>
            <button className={styles.btn} onClick={() => setOpen(true)} aria-label={`View full-size: ${alt}`}>
                <img src={src} alt={alt} className={styles.img} />
            </button>
            {open && (
                <div className={styles.overlay} onClick={() => setOpen(false)} role="dialog" aria-modal="true" aria-label={alt}>
                    <button className={styles.close} onClick={() => setOpen(false)} aria-label="Close">✕</button>
                    <img src={src} alt={alt} className={styles.lightboxImage} onClick={(e) => e.stopPropagation()} />
                </div>
            )}
        </div>
    );
}
