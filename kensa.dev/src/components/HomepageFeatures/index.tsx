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
        title: 'BDD Testing Made Simple',
        Svg: require('@site/static/img/testing.svg').default,
        description: (
            <>
                Kensa allows you to write tests in natural language, simplifying BDD testing for Kotlin and Java developers without requiring additional text descriptions.
            </>
        ),
    },
    {
        title: 'Integrated Framework Support',
        Svg: require('@site/static/img/coding.svg').default,
        description: (
            <>
                Seamlessly integrate with popular frameworks like <strong>JUnit5</strong> (available now), with <strong>TestNG</strong> and <strong>Kotest</strong> support coming soon.
            </>
        ),
    },
    {
        title: 'Rich HTML Test Output',
        Svg: require('@site/static/img/html.svg').default,
        description: (
            <>
                Write Kotlin or Java tests in your own fluent, readable style, and Kensa will generate interactive documentation directly from the source code, enriched with variable and fixture values for added clarity.
            </>
        ),
    },
    {
        title: 'Interactive Sequence Diagrams',
        Svg: require('@site/static/img/work-flow.svg').default,
        description: (
            <>
                Capture interactions from your tests and display them as embedded sequence diagrams to provide a visual representation of test workflows.
            </>
        ),
    },
    {
        title: 'Flexible Matcher Support',
        Svg: require('@site/static/img/checked.svg').default,
        description: (
            <>
                Use your preferred assertion library, including <strong>Hamcrest</strong>, <strong>HamKrest</strong>, <strong>Kotest</strong>, or <strong>AssertJ</strong>, for greater flexibility in designing tests.
            </>
        ),
    },
    {
        title: 'Field & Variable Tracking',
        Svg: require('@site/static/img/code.svg').default,
        description: (
            <>
                Effortlessly capture and display the values of variables or fields directly in the HTML report, offering unparalleled test insights.
            </>
        ),
    },
];

function Feature({ title, Svg, description }: FeatureItem) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                <Svg className={styles.featureSvg} role="img" />
            </div>
            <div className="text--center padding-horiz--md">
                <Heading as="h3">{title}</Heading>
                <p>{description}</p>
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
