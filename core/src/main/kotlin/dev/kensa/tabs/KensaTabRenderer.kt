package dev.kensa.tabs

interface KensaTabRenderer {
    /**
     * Return null (or blank) to omit the tab for this invocation.
     */
    fun render(ctx: KensaTabContext): String?

    /**
     * Rendered content for this invocation, including optional entry counts and raw records.
     *
     * Defaults to the result of [render], so a renderer that implements only [render] is unaffected.
     * A renderer that instead writes [render] in terms of this method must override it as well, or the
     * two default to each other and recurse.
     * Return null to omit the tab for this invocation.
     */
    fun renderTab(ctx: KensaTabContext): TabContent? = render(ctx)?.let { TabContent(it) }

    /**
     * MIME type of the rendered content. Defaults to "text/plain".
     * Override to return "text/html" when render() produces HTML markup.
     */
    fun mediaType(): String = "text/plain"
}

/**
 * Provides a per-invocation identifier used to correlate external data (e.g. logs) to this invocation.
 *
 * Implementations should return a stable string value (for example, a UUID) that also appears in the
 * external data source(s). Return null when no identifier is available for the invocation.
 */
fun interface InvocationIdentifierProvider {
    fun identifier(ctx: KensaTabContext): String?
}

/**
 * Default provider used when a tab does not need an invocation identifier.
 */
object NoInvocationIdentifierProvider : InvocationIdentifierProvider {
    override fun identifier(ctx: KensaTabContext): String? = null
}