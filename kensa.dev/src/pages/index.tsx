import type { ReactNode } from 'react';
import { useState } from 'react';
import clsx from 'clsx';
import Head from '@docusaurus/Head';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import HomepageShowcase from '@site/src/components/HomepageShowcase';
import HomepageEmbed from '@site/src/components/HomepageEmbed';
import HomepageEcosystem from '@site/src/components/HomepageEcosystem';
import { orderServiceTestUrl } from '@site/src/util/reportUrl';

import styles from './index.module.css';

function HomepageHeader() {
    const { siteConfig } = useDocusaurusContext();
    const [loaded, setLoaded] = useState(false);
    // The hero is always dark, whatever the site theme, so the report inside it is too.
    const reportTest = orderServiceTestUrl(String(siteConfig.customFields?.reportBase), 'dark');

    return (
        <header className={styles.hero}>
            <div className={clsx(styles.glow, styles.glowGiven)} aria-hidden="true" />
            <div className={clsx(styles.glow, styles.glowWhen)} aria-hidden="true" />
            <div className={clsx(styles.glow, styles.glowThen)} aria-hidden="true" />
            <div className={clsx('container', styles.heroInner)}>
                <div className={styles.copy}>
                    <p className={styles.eyebrow}>// Acceptance testing for Kotlin and Java</p>
                    <h1 className={styles.title}>
                        Output for everyone who <span className={styles.lit}>didn&rsquo;t</span> write the test.
                    </h1>
                    <p className={styles.strap}>
                        Given, When, Then in plain code, no feature files. The report is generated from
                        the test that ran, with real values, every message between services, and an
                        interactive sequence diagram.
                    </p>
                    <div className={styles.buttons}>
                        <Link className={styles.ctaPrimary} to="/docs/quickstart/kotlin-quickstart">
                            Get started
                        </Link>
                        <Link className={styles.ctaSecondary} href="https://github.com/kensa-dev/kensa">
                            View on GitHub
                        </Link>
                    </div>
                    <p className={styles.meta}>
                        Apache 2.0 · Kotlin 2.x · Java 17+ · JUnit 5 / 6 · Kotest · TestNG
                    </p>
                </div>

                <div className={clsx(styles.reportCard, loaded && styles.reportLoaded)}>
                    <div className={styles.reportBar}>
                        <span>OrderServiceTest · report</span>
                        <span className={styles.reportState}>passed</span>
                    </div>
                    <div className={styles.reportFrame}>
                        <p className={styles.loading} aria-hidden={loaded}>Loading the live report&hellip;</p>
                        <iframe
                            className={styles.report}
                            src={reportTest}
                            title="Live Kensa report for OrderServiceTest, from the Clearwave example"
                            referrerPolicy="no-referrer"
                            onLoad={() => setLoaded(true)}
                        />
                    </div>
                </div>
            </div>
        </header>
    );
}

export default function Home(): ReactNode {
    const { siteConfig } = useDocusaurusContext();
    return (
        <Layout
            title={siteConfig.title}
            description="BDD testing framework for Kotlin and Java — write Given–When–Then tests in code, get living HTML documentation and sequence diagrams.">
            {/*
              Docusaurus formats <title> as `${title} | ${siteTitle}` for every value
              except siteTitle itself, so the homepage can only be `Kensa` or
              `… | Kensa` via the Layout prop. This overrides it outright: the brand
              needs subject context in results (it competes with an unrelated
              heat-pump manufacturer for the bare name).
            */}
            <Head>
                <title>Kensa — BDD Testing for Kotlin &amp; Java Without Gherkin</title>
            </Head>
            <HomepageHeader />
            <main>
                <HomepageShowcase />
                <HomepageFeatures />
                <HomepageEmbed />
                <HomepageEcosystem />
            </main>
        </Layout>
    );
}
