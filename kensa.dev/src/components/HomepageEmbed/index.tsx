import type { ReactNode } from 'react';
import Link from '@docusaurus/Link';
import styles from './styles.module.css';

// A Kensa test as it appears inside someone else's page: a mock acceptance-criteria
// page in the style of a wiki, with the embedded report at the bottom. The report
// content is the Clearwave OrderServiceTest, rendered statically here.
export default function HomepageEmbed(): ReactNode {
    return (
        <section className={styles.embed}>
            <div className={styles.inner}>
                <div className={styles.copy}>
                    <p className={styles.eyebrow}>// Embed</p>
                    <h2 className={styles.heading}>Put the report where the decision is made.</h2>
                    <p className={styles.intro}>
                        Any test, chromeless, at a stable URL. Drop it into a Confluence page, a Jira
                        story, a design doc or a service catalog. The page shows what the system did,
                        and it stays current with every run.
                    </p>
                    <Link className={styles.link} to="/docs/reports/embedding">
                        Embedding a report →
                    </Link>
                </div>

                <div className={styles.page} aria-label="A wiki page with an embedded Kensa report">
                    <div className={styles.pageHead}>
                        <span className={styles.pageTitle}>Acceptance criteria</span>
                        <span>·</span>
                        <span>CW-214 Voice and broadband bundle</span>
                    </div>
                    <p className={styles.pageText}>
                        When a customer places a combined order, both suppliers must confirm before the
                        order is marked complete.
                    </p>
                    <div className={styles.card}>
                        <div className={styles.cardHead}>
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                <circle cx="12" cy="12" r="9" />
                                <path d="M8.5 12.5l2.5 2.5 4.5-5" />
                            </svg>
                            voice and broadband order is successfully completed
                        </div>
                        <div className={styles.cardBody}>
                            <div className={styles.sentence}><span className={styles.given}>Given</span><span>open network will complete the order</span></div>
                            <div className={styles.sentence}><span className={styles.when}>When</span><span>a voice and broadband order is placed</span></div>
                            <div className={styles.sentence}><span className={styles.then}>Then</span><span>eventually all notifications should show both suppliers completed successfully</span></div>
                        </div>
                        <div className={styles.cardFoot}>
                            <span>kensa · OrderServiceTest · latest run</span>
                            <span className={styles.then}>passed</span>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
