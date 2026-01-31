package dev.kensa.tabs

interface KensaTabRenderer {
    /**
     * Return null (or blank) to omit the tab for this invocation.
     */
    fun render(ctx: KensaTabContext): String?
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