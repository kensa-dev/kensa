import type { ReactNode } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import HomepageShowcase from '@site/src/components/HomepageShowcase';
import HomepageEcosystem from '@site/src/components/HomepageEcosystem';

import styles from './index.module.css';

function HomepageHeader() {
    const { siteConfig } = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <div className={styles.heroInner}>
                    <div className={styles.heroTitle}>
                        <span className={styles.heroLogoDisc}>
                            <img
                                src="/img/kensa-mark-white.svg"
                                alt="Kensa Logo"
                                className={styles.heroLogo}
                                width={56}
                                height={56}
                            />
                        </span>
                        <h1 className={styles.title}>{siteConfig.title}</h1>
                    </div>
                    <p className={styles.tagline}>
                        BDD for Kotlin &amp; Java — write tests that read like requirements,
                        get reports that speak for themselves.
                    </p>
                    <div className={styles.badgeRow}>
                        <img
                            src="https://img.shields.io/github/v/release/kensa-dev/kensa?style=flat-square&color=3cad6e&labelColor=1a3a2a"
                            alt="Latest release"
                            width={94}
                            height={20}
                        />
                        <img
                            src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=flat-square&logo=kotlin&logoColor=white"
                            alt="Kotlin 2.x"
                            width={85}
                            height={20}
                        />
                        <img
                            src="https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white"
                            alt="Java 17+"
                            width={85}
                            height={20}
                        />
                        <img
                            src="https://img.shields.io/badge/JUnit%205%2F6%20%7C%20Kotest%20%7C%20TestNG-supported-2e8555?style=flat-square"
                            alt="Framework support"
                            width={226}
                            height={20}
                        />
                    </div>
                    <div className={styles.buttons}>
                        <Link
                            className={clsx('button button--secondary button--lg', styles.ctaPrimary)}
                            to="/docs/quickstart/kotlin-quickstart">
                            Get Started
                        </Link>
                        <Link
                            className={clsx('button button--outline button--secondary button--lg', styles.githubButton)}
                            href="https://github.com/kensa-dev/kensa">
                            View on GitHub
                        </Link>
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
            <HomepageHeader />
            <main>
                <HomepageShowcase />
                <HomepageFeatures />
                <HomepageEcosystem />
            </main>
        </Layout>
    );
}
