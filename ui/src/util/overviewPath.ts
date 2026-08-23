export function overviewPathFor(search: string, sourceId: string): string {
    const params = new URLSearchParams(search);
    // The overview is a source-level view, so any test-level selection carried in
    // the current query would point at nothing once we get there.
    params.delete('method');
    params.delete('invocation');
    params.set('source', sourceId);
    return `/overview?${params.toString()}`;
}
