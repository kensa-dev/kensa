import type { ReactNode } from 'react';
import clsx from 'clsx';
import Head from '@docusaurus/Head';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import HomepageShowcase from '@site/src/components/HomepageShowcase';
import HomepageEmbed from '@site/src/components/HomepageEmbed';
import HomepageEcosystem from '@site/src/components/HomepageEcosystem';
import { feasibilityEmbedUrl, feasibilityTestUrl } from '@site/src/util/reportUrl';
import HomepageInstall from '@site/src/components/HomepageInstall';
import ReportFrame from '@site/src/components/ReportFrame';

import styles from './index.module.css';

function HomepageHeader() {
    const { siteConfig } = useDocusaurusContext();
    // The hero is always dark, whatever the site theme, so the report inside it is too.
    const reportBase = String(siteConfig.customFields?.reportBase);
    const reportEmbed = feasibilityEmbedUrl(reportBase, 'dark');
    const reportTest = feasibilityTestUrl(reportBase, 'dark');

    return (
        <header className={styles.hero}>
            {/* Sizes every iframe[data-kensa-embed] on the page to the embed's own height. */}
            <Head>
                <script src={`${reportBase}kensa-embed.js`} />
            </Head>
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
                        Given, When, Then in the test. Nothing else to keep in sync. The report is
                        generated from the test that ran, with real values, every message between
                        services, and an interactive sequence diagram.
                    </p>
                    <div className={styles.buttons}>
                        <Link className={styles.ctaPrimary} to="/docs/category/quickstart-guide">
                            Get started
                        </Link>
                        <a className={styles.ctaSecondary} href={reportTest} target="_blank" rel="noopener noreferrer">
                            Browse a real report
                        </a>
                    </div>
                    <p className={styles.meta}>
                        Apache 2.0 · Kotlin 2.x · Java 17+ · JUnit 5 / 6 · Kotest · TestNG
                        <br />
                        In production acceptance tests at a large UK telco.
                    </p>
                </div>

                <div className={styles.reportCard}>
                    <ReportFrame
                        embedSrc={reportEmbed}
                        fullUrl={reportTest}
                        picture="/img/report/feasibility-dark.png"
                        alt="The FeasibilityServiceTest method 'address is serviceable by both suppliers' in the Kensa report: a sequence diagram between Customer, FeasibilityService, OpenNetwork and FibreVision, then the Given, When, Then sentences with their values"
                        title="Live Kensa report for FeasibilityServiceTest, from the Clearwave example"
                    />
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
                <HomepageInstall />
                <HomepageFeatures />
                <HomepageEmbed />
                <HomepageEcosystem />
            </main>
        </Layout>
    );
}
