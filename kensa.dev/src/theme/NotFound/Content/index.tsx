import type { ReactNode } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Heading from '@theme/Heading';
import type { Props } from '@theme/NotFound/Content';
import styles from './styles.module.css';

const destinations = [
    { to: '/docs/intro', label: 'Introduction', note: 'What Kensa is and what the report shows' },
    { to: '/docs/category/quickstart-guide', label: 'Quickstart', note: 'Kotlin, Java, Kotest, TestNG or Maven' },
    { to: '/docs/examples', label: 'Example projects', note: 'Complete suites with live reports' },
    { to: '/blog', label: 'Blog', note: 'BDD without Gherkin, and more' },
];

export default function NotFoundContent({ className }: Props): ReactNode {
    return (
        <main className={clsx('container', styles.notFound, className)}>
            <p className={styles.eyebrow}>// 404</p>
            <Heading as="h1" className={styles.heading}>
                No page at this address.
            </Heading>
            <p className={styles.intro}>
                It may have moved when the docs were reorganised. Search from the bar above, or pick
                up from one of these.
            </p>
            <ul className={styles.links}>
                {destinations.map(({ to, label, note }) => (
                    <li key={to}>
                        <Link to={to} className={styles.link}>
                            <span className={styles.label}>{label} →</span>
                            <span className={styles.note}>{note}</span>
                        </Link>
                    </li>
                ))}
            </ul>
        </main>
    );
}
