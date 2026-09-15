// Where copied links point. A report published at a stable address (a CI
// artifact URL such as TeamCity's .lastSuccessful, or any published site) tells
// the UI that base as `linkBaseUrl`; without it, links use the page's own
// location as before. A base ending in `/` names a directory, so index.html
// is appended to keep the hash route on the report page.

export interface LinkBaseConfig {
    linkBaseUrl?: string | null;
}

export interface LocationLike {
    origin: string;
    pathname: string;
    search: string;
}

export function reportBase(config: LinkBaseConfig, location: LocationLike): string {
    const base = config.linkBaseUrl?.trim();
    if (base) return base.endsWith('/') ? `${base}index.html` : base;
    return `${location.origin}${location.pathname}${location.search}`;
}
