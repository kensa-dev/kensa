import type { ReactNode } from 'react';
import Link from '@docusaurus/Link';
import styles from './styles.module.css';

export default function HomepagePlugin(): ReactNode {
    return (
        <section className={styles.pluginSection}>
            <div className="container">
                <div className={styles.pluginInner}>
                    <div className={styles.pluginCopy}>
                        <div className={styles.ideLabel}>
                            <svg className={styles.ideIcon} viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <rect x="2" y="2" width="20" height="20" rx="4" fill="currentColor" opacity="0.15"/>
                                <path d="M6 17V7h2.5l2.5 4 2.5-4H16v10h-2V10.5l-2 3.2-2-3.2V17H6z" fill="currentColor"/>
                                <rect x="6" y="18.5" width="5" height="1.5" rx="0.75" fill="currentColor"/>
                            </svg>
                            IntelliJ Plugin
                        </div>
                        <h2 className={styles.pluginHeading}>
                            Open reports from your IDE
                        </h2>
                        <p className={styles.pluginDesc}>
                            Open any Kensa test report in your browser directly from IntelliJ IDEA.
                            No hunting through the filesystem — just click and the report opens.
                        </p>
                        <ul className={styles.pluginFeatures}>
                            <li>Gutter icons on test functions</li>
                            <li>Links in the test console output</li>
                            <li>Test toolbar actions</li>
                        </ul>
                        <Link
                            className={styles.pluginCta}
                            href="https://plugins.jetbrains.com/plugin/31099">
                            View on JetBrains Marketplace →
                        </Link>
                    </div>
                    <div className={styles.pluginEmbed}>
                        <iframe
                            width="384"
                            height="319"
                            src="https://plugins.jetbrains.com/embeddable/card/31099"
                            title="Kensa IntelliJ Plugin on JetBrains Marketplace"
                            frameBorder="0"
                        />
                    </div>
                </div>
            </div>
        </section>
    );
}
