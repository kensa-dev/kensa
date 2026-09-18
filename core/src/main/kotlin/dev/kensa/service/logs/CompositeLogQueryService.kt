package dev.kensa.service.logs

/**
 * Delegates queries to different LogQueryService implementations depending on sourceId.
 */
internal class CompositeLogQueryService(
    private val delegatesBySourceId: Map<String, LogQueryService>
) : LogQueryService {

    override fun query(sourceId: String, identifier: String): List<LogRecord> =
        delegateFor(sourceId).query(sourceId, identifier)

    override fun queryAll(sourceId: String): List<LogRecord> =
        delegateFor(sourceId).queryAll(sourceId)

    /**
     * A delegate serving several sources is registered under each of their ids, so it reports the
     * same sources once per registration. The first mention of an id wins, keeping registration order.
     */
    override fun sources(): List<LogSource> =
        delegatesBySourceId.values.flatMap { it.sources() }.distinctBy { it.id }

    private fun delegateFor(sourceId: String): LogQueryService =
        delegatesBySourceId[sourceId]
            ?: error("No LogQueryService registered for sourceId [$sourceId]")
}