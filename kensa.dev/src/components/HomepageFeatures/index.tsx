import type { ReactNode } from 'react';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
    title: string;
    Svg: React.ComponentType<React.ComponentProps<'svg'>>;
    description: ReactNode;
};

// Three cards, one per reader. The framework matrix and the assertion-library
// list live in the quickstarts; this is the first-screen sell.
const FeatureList: FeatureItem[] = [
    {
        title: 'For the people who can’t read the code',
        Svg: require('@site/static/img/html.svg').default,
        description: (
            <>
                Testers, analysts and product owners get a report that says what the system did:
                Given&ndash;When&ndash;Then sentences with this run&rsquo;s real values, every message
                that crossed between services, and a sequence diagram drawn from them. Open any
                interaction and read the payload.
            </>
        ),
    },
    {
        title: 'For the developer writing it',
        Svg: require('@site/static/img/coding.svg').default,
        description: (
            <>
                Ordinary Kotlin or Java on <strong>JUnit 5 &amp; 6</strong>, <strong>Kotest</strong> or{' '}
                <strong>TestNG</strong>, with the assertions you already use. No feature files, no step
                definitions, nothing to keep in sync. Refactor and the report follows, because the
                report is generated from the test that ran.
            </>
        ),
    },
    {
        title: 'For the system you actually ship',
        Svg: require('@site/static/img/work-flow.svg').default,
        description: (
            <>
                Built for acceptance tests that sit outside a deployed application. Push a message in,
                watch what comes out, and let the report show the traffic. Link each test to its
                ticket with <code>@Issue</code> and the specification, the test and the evidence are
                one thing.
            </>
        ),
    },
];

function Feature({ title, Svg, description }: FeatureItem) {
    return (
        <div className={styles.featureCard}>
            <Svg className={styles.featureSvg} role="img" />
            <Heading as="h3" className={styles.featureTitle}>{title}</Heading>
            <p className={styles.featureDesc}>{description}</p>
        </div>
    );
}

export default function HomepageFeatures(): ReactNode {
    return (
        <section className={styles.features}>
            <div className="container">
                <h2 className="sr-only">Who Kensa is for</h2>
                <div className={styles.grid}>
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
