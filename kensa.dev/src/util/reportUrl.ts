// The live Clearwave example report, opened on one test. Report routes are
// hash-based: #/test/<source>::<class>?method=<name>. `theme` matches the report
// to the surrounding page (honoured by the report UI from the release after 0.9.3).
export type ReportTheme = 'light' | 'dark';

export const orderServiceTestUrl = (base: string, theme: ReportTheme): string =>
    `${base}#/test/test::com.clearwave.OrderServiceTest` +
    `?method=${encodeURIComponent('voice and broadband order is successfully completed')}` +
    `&theme=${theme}`;
