// The live Clearwave example report, opened on one test. Report routes are
// hash-based: #/test/<source>::<class>?method=<name>. `theme` matches the report
// to the surrounding page (honoured by the report UI from the release after 0.9.3).
export type ReportTheme = 'light' | 'dark';

// The full report, opened on one test class or method.
export const testUrl = (base: string, testClass: string, method: string | null, theme: ReportTheme): string =>
    `${base}#/test/test::${testClass}` +
    (method === null ? '?' : `?method=${encodeURIComponent(method)}&`) +
    `theme=${theme}`;

export const orderServiceTestUrl = (base: string, theme: ReportTheme): string =>
    testUrl(base, 'com.clearwave.OrderServiceTest', 'voice and broadband order is successfully completed', theme);

export const feasibilityTestUrl = (base: string, theme: ReportTheme): string =>
    testUrl(base, 'com.clearwave.FeasibilityServiceTest', 'address is serviceable by both suppliers', theme);

// A test class, or one method of it, in embed mode: no sidebar, header or search,
// just the cards and a footer linking to the full report. kensa-embed.js on the
// homepage sizes each frame to the embed's own height.
export const embedUrl = (base: string, testClass: string, method: string | null, theme: ReportTheme): string =>
    `${base}#/embed/test::${testClass}` +
    (method === null ? '?' : `?method=${encodeURIComponent(method)}&`) +
    `theme=${theme}`;

export const orderServiceEmbedUrl = (base: string, theme: ReportTheme): string =>
    embedUrl(base, 'com.clearwave.OrderServiceTest', 'voice and broadband order is successfully completed', theme);

// The hero shows one short feasibility scenario, so the frame fits without scrolling.
export const feasibilityEmbedUrl = (base: string, theme: ReportTheme): string =>
    embedUrl(base, 'com.clearwave.FeasibilityServiceTest', 'address is serviceable by both suppliers', theme);
