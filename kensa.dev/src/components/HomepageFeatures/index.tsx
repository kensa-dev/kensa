import type { ReactNode } from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
    title: string;
    Svg: React.ComponentType<React.ComponentProps<'svg'>>;
    description: ReactNode;
};

const FeatureList: FeatureItem[] = [
    {
        title: 'No Gherkin. Just code.',
        Svg: require('@site/static/img/testing.svg').default,
        description: (
            <>Write Given–When–Then tests directly in Kotlin or Java. No feature files, no step definitions to maintain.</>
        ),
    },
    {
        title: 'Works with your framework',
        Svg: require('@site/static/img/coding.svg').default,
        description: (
            <>Supports <strong>JUnit 5 &amp; 6</strong>, <strong>Kotest</strong>, and <strong>TestNG</strong>. Auto-registered via ServiceLoader — add the dependency and you're done.</>
        ),
    },
    {
        title: 'Living HTML reports',
        Svg: require('@site/static/img/html.svg').default,
        description: (
            <>Every test run produces an interactive HTML report with test sentences, captured values, and fixture state — generated from source code, not annotations.</>
        ),
    },
    {
        title: 'Sequence diagrams included',
        Svg: require('@site/static/img/work-flow.svg').default,
        description: (
            <>Capture interactions between components in your tests. Kensa renders them as sequence diagrams embedded directly in the report.</>
        ),
    },
    {
        title: 'Your assertion library',
        Svg: require('@site/static/img/checked.svg').default,
        description: (
            <>Use <strong>AssertJ</strong>, <strong>Hamcrest</strong>, <strong>HamKrest</strong>, or <strong>Kotest</strong> matchers. Mix them freely within a project.</>
        ),
    },
    {
        title: 'Values in the report',
        Svg: require('@site/static/img/code.svg').default,
        description: (
            <>Annotate fields with <code>@RenderedValue</code> and their values appear in the report automatically — no logging, no boilerplate.</>
        ),
    },
];

function Feature({ title, Svg, description }: FeatureItem) {
    return (
        <div className={clsx('col col--4', styles.featureCol)}>
            <div className={styles.featureCard}>
                <Svg className={styles.featureSvg} role="img" />
                <Heading as="h3" className={styles.featureTitle}>{title}</Heading>
                <p className={styles.featureDesc}>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures(): ReactNode {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
