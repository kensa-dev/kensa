import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <img
                    src="/img/Logo.svg"
                    alt="Kensa Logo"
                    className={styles.invertLogo}
                    style={{ width: '100px', height: '100px', marginBottom: '1rem' }}
                />
                <Heading as="h1" className="hero__title">
                    {siteConfig.title}
                </Heading>
                <img src="https://img.shields.io/github/v/release/kensa-dev/kensa" alt="Kensa Release Version"/>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <div className={styles.buttons}>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/intro">
                        Kensa Tutorial - 5min ⏱️
                    </Link>
                </div>
            </div>
        </header>
    );
}

export default function Home(): ReactNode {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={siteConfig.title}
            description="BDD Testing Framework for Kotlin & Java">
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
