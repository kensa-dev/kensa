import type { ReactNode } from 'react';
import Link from '@docusaurus/Link';
import styles from './styles.module.css';

type IntegrationCard = {
    badge: string;
    title: string;
    description: string;
    features: string[];
    marketplaceUrl: string;
    marketplaceLabel: string;
    docsUrl?: string;
    Icon: ReactNode;
};

const IdeIcon = (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <rect x="2" y="2" width="20" height="20" rx="4" fill="currentColor" opacity="0.15"/>
        <path d="M6 17V7h2.5l2.5 4 2.5-4H16v10h-2V10.5l-2 3.2-2-3.2V17H6z" fill="currentColor"/>
        <rect x="6" y="18.5" width="5" height="1.5" rx="0.75" fill="currentColor"/>
    </svg>
);

const CiIcon = (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <rect x="2" y="2" width="20" height="20" rx="4" fill="currentColor" opacity="0.15"/>
        <path d="M7 8h10M7 12h10M7 16h6" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"/>
        <circle cx="17.5" cy="16" r="1.6" fill="currentColor"/>
    </svg>
);

const integrations: IntegrationCard[] = [
    {
        badge: 'IntelliJ Plugin',
        title: 'Open reports from your IDE',
        description: 'Open any Kensa test report in your browser directly from IntelliJ IDEA.',
        features: [
            'Gutter icons on test functions',
            'Links in test console output',
            'Test toolbar actions',
        ],
        marketplaceUrl: 'https://plugins.jetbrains.com/plugin/31099',
        marketplaceLabel: 'View on JetBrains Marketplace',
        docsUrl: '/docs/integrations/intellij-plugin',
        Icon: IdeIcon,
    },
    {
        badge: 'TeamCity Plugin',
        title: 'Reports inside your CI',
        description: 'Embed Kensa reports, GWT test names, and failure narratives directly in TeamCity builds.',
        features: [
            'Kensa Report tab on every build',
            'Given-When-Then test display names',
            'Failure summaries with captured state',
        ],
        marketplaceUrl: 'https://plugins.jetbrains.com/plugin/31628',
        marketplaceLabel: 'View on JetBrains Marketplace',
        docsUrl: '/docs/integrations/teamcity-plugin',
        Icon: CiIcon,
    },
];

function Card({ badge, title, description, features, marketplaceUrl, marketplaceLabel, docsUrl, Icon }: IntegrationCard) {
    return (
        <article className={styles.card}>
            <div className={styles.cardBadge}>
                <span className={styles.cardIcon} aria-hidden="true">{Icon}</span>
                {badge}
            </div>
            <h3 className={styles.cardTitle}>{title}</h3>
            <p className={styles.cardDesc}>{description}</p>
            <ul className={styles.cardFeatures}>
                {features.map((f) => <li key={f}>{f}</li>)}
            </ul>
            <div className={styles.cardLinks}>
                <Link className={styles.cardCta} href={marketplaceUrl}>
                    {marketplaceLabel} →
                </Link>
                {docsUrl && (
                    <Link className={styles.cardSecondary} to={docsUrl}>
                        Docs
                    </Link>
                )}
            </div>
        </article>
    );
}

export default function HomepageEcosystem(): ReactNode {
    return (
        <section className={styles.ecosystem}>
            <div className="container">
                <div className={styles.header}>
                    <h2 className={styles.heading}>Plugins &amp; Integrations</h2>
                    <p className={styles.subheading}>
                        Surface Kensa output where your team already works.
                    </p>
                </div>
                <div className={styles.grid}>
                    {integrations.map((card) => (
                        <Card key={card.badge} {...card} />
                    ))}
                </div>
            </div>
        </section>
    );
}
