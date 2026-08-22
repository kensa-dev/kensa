// Reports written before the issue tracker became nullable carry this sentinel rather than
// an absent value. Treat it as unconfigured so old bundles do not render dead links.
const LEGACY_UNSET_SENTINEL = "http://empty";

export function issueHref(trackerUrl: string | null | undefined, issue: string): string | null {
    const trimmed = trackerUrl?.trim();
    if (!trimmed || trimmed === LEGACY_UNSET_SENTINEL) return null;

    return trimmed.endsWith("/") ? `${trimmed}${issue}` : `${trimmed}/${issue}`;
}
