import type { ReactNode } from 'react';
import CodeBlock from '@theme/CodeBlock';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './styles.module.css';

// The whole setup for a Kotlin Gradle project, with the current versions filled in.
// The compiler plugin is only behind @RenderedValue / @ExpandableSentence; the
// dependencies alone produce the report.
export default function HomepageInstall(): ReactNode {
    const { siteConfig } = useDocusaurusContext();
    const kensaVersion = String(siteConfig.customFields?.kensaVersion);
    const pluginVersion = String(siteConfig.customFields?.kensaPluginVersion);
    const snippet = `plugins {
    id("dev.kensa.gradle-plugin") version "${pluginVersion}"
}

dependencies {
    testImplementation(platform("dev.kensa:kensa-bom:${kensaVersion}"))
    testImplementation("dev.kensa:kensa-framework-junit6")
    testImplementation("dev.kensa:kensa-assertions-kotest")
}`;

    return (
        <section className={styles.install}>
            <div className={`container ${styles.inner}`}>
                <div className={styles.copy}>
                    <p className={styles.eyebrow}>// Install</p>
                    <h2 className={styles.heading}>Two blocks in the build file.</h2>
                    <p className={styles.intro}>
                        The dependencies alone give you the sentences, every captured message and the
                        sequence diagram. The Gradle plugin adds the Kotlin compiler plugin behind{' '}
                        <code>@RenderedValue</code> and <code>@ExpandableSentence</code>, and site mode
                        across sourcesets. A Java project needs only the dependencies.
                    </p>
                    <p className={styles.links}>
                        <Link to="/docs/quickstart/java-quickstart">Java</Link>
                        <Link to="/docs/quickstart/kotest-quickstart">Kotest</Link>
                        <Link to="/docs/quickstart/testng-quickstart">TestNG</Link>
                        <Link to="/docs/quickstart/maven-quickstart">Maven</Link>
                    </p>
                </div>
                <div className={styles.code}>
                    <CodeBlock language="kotlin" title="build.gradle.kts">{snippet}</CodeBlock>
                </div>
            </div>
        </section>
    );
}
