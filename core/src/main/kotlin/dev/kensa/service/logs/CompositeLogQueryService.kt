package dev.kensa.service.logs

/**
 * Delegates queries to different LogQueryService implementations depending on sourceId.
 */
class CompositeLogQueryService(
    private val delegatesBySourceId: Map<String, LogQueryService>
) : LogQueryService {

    override fun query(sourceId: String, identifier: String): List<LogRecord> =
        delegateFor(sourceId).query(sourceId, identifier)

    override fun queryAll(sourceId: String): List<LogRecord> =
        delegateFor(sourceId).queryAll(sourceId)

    private fun delegateFor(sourceId: String): LogQueryService =
        delegatesBySourceId[sourceId]
            ?: error("No LogQueryService registered for sourceId [$sourceId]")
}