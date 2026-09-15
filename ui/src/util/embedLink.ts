import {embedHash} from './anchorLink';
import {reportBase, type LinkBaseConfig, type LocationLike} from './linkBase';

export interface EmbedLinkConfig extends LinkBaseConfig {
    unfurl?: boolean;
}

// The link Copy embed link hands out. A report written with a linkBaseUrl has
// a static page per class and method under embed/ in its bundle, carrying the
// Open Graph card a chat or tracker shows for a pasted link and a refresh on
// to the live embed, so the copied link points at that page. Otherwise, and
// for an invocation, which has no page, it is the hash route as before.
export function embedLinkFor(
    config: EmbedLinkConfig,
    location: LocationLike,
    sourceBaseUrl: string,
    testId: string,
    method?: string,
    invocation?: number,
): string {
    const page = reportBase(config, location);
    if (!config.unfurl || invocation !== undefined) return `${page}${embedHash(testId, method, invocation)}`;
    const dir = page.slice(0, page.lastIndexOf('/') + 1);
    const source = sourceBaseUrl.replace(/^\.?\/?/, '').replace(/\/$/, '');
    const separator = testId.indexOf('::');
    const className = separator < 0 ? testId : testId.slice(separator + 2);
    const file = method ? `${encodeURIComponent(method)}.html` : 'index.html';
    return `${dir}${source ? `${source}/` : ''}embed/${className}/${file}`;
}
