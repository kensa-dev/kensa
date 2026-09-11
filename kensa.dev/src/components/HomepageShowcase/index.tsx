import type { ReactNode } from 'react';
import { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import CodeBlock from '@theme/CodeBlock';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import { useColorMode } from '@docusaurus/theme-common';
import styles from './styles.module.css';

// The live Clearwave example report, opened on the test shown in the code
// block above it. Report routes are hash-based: #/test/<source>::<class>?method=<name>.
// `theme` matches the report to the site's colour mode (honoured by the report UI from
// the release after 0.9.3; ignored by earlier builds).
const reportTestUrl = (base: string, theme: 'light' | 'dark') =>
    `${base}#/test/test::com.clearwave.OrderServiceTest` +
    `?method=${encodeURIComponent('voice and broadband order is successfully completed')}` +
    `&theme=${theme}`;

// Verbatim from clearwave-example/src/test/kotlin/com/clearwave/OrderServiceTest.kt.
const TEST_SOURCE = `@Test
fun \`voice and broadband order is successfully completed\`() {
    given(openNetworkWillCompleteTheOrder())
    and(fibreVisionWillCompleteTheOrder())

    whenever(aVoiceAndBroadbandOrderIsPlaced())

    then(theOrderConfirmation(), shouldBePending())
    thenEventuallyAllNotifications(
        shouldShowBothSuppliersCompletedSuccessfully(
            voiceSupplier = fixtures[voiceSupplier],
            broadbandSupplier = fixtures[broadbandSupplier],
        )
    )
}`;

// The report focuses an element as it loads, and Firefox scrolls the parent
// page to bring a focused element into view, frame or not. So the iframe is
// only mounted once the frame is already on screen: by then any focus scroll
// lands where the reader is looking. Falls back to mounting immediately where
// IntersectionObserver is unavailable.
function useOnScreen<T extends Element>(): [React.RefObject<T | null>, boolean] {
    const ref = useRef<T>(null);
    const [onScreen, setOnScreen] = useState(false);

    useEffect(() => {
        const el = ref.current;
        if (!el) return;
        if (typeof IntersectionObserver === 'undefined') {
            setOnScreen(true);
            return;
        }
        const observer = new IntersectionObserver(
            (entries) => {
                if (entries.some((e) => e.isIntersecting)) {
                    setOnScreen(true);
                    observer.disconnect();
                }
            },
            { threshold: 0.25 },
        );
        observer.observe(el);
        return () => observer.disconnect();
    }, []);

    return [ref, onScreen];
}

export default function HomepageShowcase(): ReactNode {
    const [loaded, setLoaded] = useState(false);
    const [frameRef, frameOnScreen] = useOnScreen<HTMLDivElement>();
    const { siteConfig } = useDocusaurusContext();
    const { colorMode } = useColorMode();
    const reportTest = reportTestUrl(String(siteConfig.customFields?.reportBase), colorMode);

    return (
        <section className={styles.showcase}>
            <div className="container">
                <h2 className={styles.eyebrow}>See it in action</h2>
                <p className={styles.intro}>
                    This is a real test from the Clearwave example, and below it the report it
                    produced, live. Click around: open an interaction, read a payload, follow a
                    value back to its fixture.
                </p>

                <p className={styles.label}>Write this&hellip;</p>
                <div className={styles.code}>
                    <CodeBlock language="kotlin" title="OrderServiceTest.kt">{TEST_SOURCE}</CodeBlock>
                </div>

                <p className={styles.label}>&hellip;get this</p>
                <div ref={frameRef} className={clsx(styles.frame, loaded && styles.frameLoaded)}>
                    <p className={styles.loading} aria-hidden={loaded}>Loading the live report&hellip;</p>
                    {frameOnScreen && (
                        <iframe
                            key={reportTest}
                            className={styles.report}
                            src={reportTest}
                            title="Live Kensa report for OrderServiceTest, from the Clearwave example"
                            referrerPolicy="no-referrer"
                            onLoad={() => setLoaded(true)}
                        />
                    )}
                </div>
                <p className={styles.liveLink}>
                    <a href={reportTest} target="_blank" rel="noopener noreferrer">
                        Open the full report in a new tab →
                    </a>
                </p>
            </div>
        </section>
    );
}
