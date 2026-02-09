package dev.kensa.service.logs

/**
 * Delegates queries to different LogQueryService implementations depending on sourceId.
 */
class CompositeLogQueryService(
    private val delegatesBySourceId: Map<String, LogQueryService>
) : LogQueryService {

    override fun query(sourceId: String, identifier: String): List<LogRecord> =
        delegatesBySourceId[sourceId]?.query(sourceId, identifier).orEmpty()

    override fun queryAll(sourceId: String): List<LogRecord> =
        delegatesBySourceId[sourceId]?.queryAll(sourceId).orEmpty()
}