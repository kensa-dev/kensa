import type { ReactNode } from 'react';
import CodeBlock from '@theme/CodeBlock';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import { useColorMode } from '@docusaurus/theme-common';
import { orderServiceTestUrl } from '@site/src/util/reportUrl';
import styles from './styles.module.css';

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

type Keyword = 'Given' | 'And' | 'When' | 'Then';

const keywordClass: Record<Keyword, string> = {
    Given: styles.given,
    And: styles.given,
    When: styles.when,
    Then: styles.then,
};

function Sentence({ keyword, children }: { keyword: Keyword; children: ReactNode }) {
    return (
        <div className={styles.sentence}>
            <span className={keywordClass[keyword]}>{keyword}</span>
            <span>{children}</span>
        </div>
    );
}

export default function HomepageShowcase(): ReactNode {
    const { siteConfig } = useDocusaurusContext();
    const { colorMode } = useColorMode();
    const reportTest = orderServiceTestUrl(String(siteConfig.customFields?.reportBase), colorMode);

    return (
        <section className={styles.showcase}>
            <div className="container">
                <p className={styles.eyebrow}>// See it in action</p>
                <h2 className={styles.heading}>Write this. Get this.</h2>
                <p className={styles.intro}>
                    A real test from the Clearwave example, and the sentences the report produced
                    from it. The method names become the words; the values come from the run.
                </p>

                <div className={styles.pair}>
                    <div className={styles.code}>
                        <CodeBlock language="kotlin" title="OrderServiceTest.kt">{TEST_SOURCE}</CodeBlock>
                    </div>
                    <div className={styles.arrow} aria-hidden="true">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M5 12h14M13 6l6 6-6 6" />
                        </svg>
                    </div>
                    <div className={styles.rendered}>
                        <div className={styles.renderedBar}>report · from the run</div>
                        <div className={styles.renderedBody}>
                            <div className={styles.testName}>
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                    <circle cx="12" cy="12" r="9" />
                                    <path d="M8.5 12.5l2.5 2.5 4.5-5" />
                                </svg>
                                voice and broadband order is successfully completed
                            </div>
                            <Sentence keyword="Given">open network will complete the order</Sentence>
                            <Sentence keyword="And">fibre vision will complete the order</Sentence>
                            <Sentence keyword="When">a voice and broadband order is placed</Sentence>
                            <Sentence keyword="Then">the order confirmation should be pending</Sentence>
                            <Sentence keyword="Then">
                                eventually all notifications should show both suppliers completed successfully
                            </Sentence>
                            <div className={styles.values}>
                                <div>voice supplier = <code>OpenNetwork</code></div>
                                <div>broadband supplier = <code>FibreVision</code></div>
                            </div>
                        </div>
                        <div className={styles.renderedFoot}>
                            <span>12 interactions captured, 18 fixtures, and a sequence diagram, in the full report</span>
                        </div>
                    </div>
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
