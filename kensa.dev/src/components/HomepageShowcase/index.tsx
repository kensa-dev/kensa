import type { ReactNode } from 'react';
import CodeBlock from '@theme/CodeBlock';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import { useColorMode } from '@docusaurus/theme-common';
import { orderServiceEmbedUrl, orderServiceTestUrl } from '@site/src/util/reportUrl';
import ReportFrame from '@site/src/components/ReportFrame';
import styles from './styles.module.css';

// Verbatim from clearwave-example/src/test/kotlin/com/clearwave/OrderServiceTest.kt.
const KOTLIN_SOURCE = `@Test
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

// Verbatim from clearwave-example/src/test/java/com/clearwave/OrderServiceJavaTest.java.
const JAVA_SOURCE = `@Test
void voiceAndBroadbandOrderIsSuccessfullyCompleted() {
    given(openNetworkWillCompleteTheOrder());
    and(fibreVisionWillCompleteTheOrder());

    whenever(aVoiceAndBroadbandOrderIsPlaced());

    then(theOrderConfirmation(), shouldBePending());
    thenEventually(Duration.ofSeconds(10), allNotifications(), shouldShowBothSuppliersCompletedSuccessfully(
        fixtures(VOICE_SUPPLIER),
        fixtures(BROADBAND_SUPPLIER)
    ));
}`;

export default function HomepageShowcase(): ReactNode {
    const { siteConfig } = useDocusaurusContext();
    const { colorMode } = useColorMode();
    const reportBase = String(siteConfig.customFields?.reportBase);
    const reportTest = orderServiceTestUrl(reportBase, colorMode);
    const reportEmbed = orderServiceEmbedUrl(reportBase, colorMode);

    return (
        <section className={styles.showcase}>
            <div className="container">
                <p className={styles.eyebrow}>// See it in action</p>
                <h2 className={styles.heading}>Write this. Get this.</h2>
                <p className={styles.intro}>
                    A real test from the Clearwave example, in Kotlin or Java, and the report it produced,
                    embedded live from the latest run. The method names become the words; the values come
                    from the run.
                </p>

                <div className={styles.pair}>
                    <div className={styles.code}>
                        <Tabs groupId="lang">
                            <TabItem value="kotlin" label="Kotlin">
                                <CodeBlock language="kotlin" title="OrderServiceTest.kt">{KOTLIN_SOURCE}</CodeBlock>
                            </TabItem>
                            <TabItem value="java" label="Java">
                                <CodeBlock language="java" title="OrderServiceJavaTest.java">{JAVA_SOURCE}</CodeBlock>
                            </TabItem>
                        </Tabs>
                    </div>
                    <div className={styles.arrow} aria-hidden="true">
                        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M5 12h14M13 6l6 6-6 6" />
                        </svg>
                    </div>
                    <div className={styles.rendered}>
                        <ReportFrame
                            embedSrc={reportEmbed}
                            fullUrl={reportTest}
                            picture={colorMode === 'dark' ? '/img/report/order-dark.png' : '/img/report/order-light.png'}
                            alt="The OrderServiceTest method 'voice and broadband order is successfully completed' in the Kensa report: a sequence diagram between Customer, OrderService, OpenNetwork and FibreVision, then the Given, When, Then sentences with the supplier values"
                            title="The OrderServiceTest method, embedded from the Clearwave example report"
                        />
                    </div>
                </div>

                <p className={styles.constraint}>
                    No <code>.feature</code> files. No step definitions. No glue to keep in sync.
                    The test is the spec. The report is the evidence.
                </p>
                <p className={styles.liveLink}>
                    <a href={reportTest} target="_blank" rel="noopener noreferrer">
                        Open the full report in a new tab →
                    </a>
                </p>
            </div>
        </section>
    );
}
