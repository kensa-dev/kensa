package dev.kensa.service.logs

class LogQueryServiceRegistry {
    private val delegatesBySourceId = linkedMapOf<String, LogQueryService>()

    fun register(id: String, service: LogQueryService) {
        delegatesBySourceId[id] = service
    }

    fun register(id: String, factory: (sourceId: String) -> LogQueryService) {
        delegatesBySourceId[id] = factory(id)
    }

    fun build(): LogQueryService =
        CompositeLogQueryService(delegatesBySourceId.toMap())

    companion object {
        fun compositeLogQueryService(block: LogQueryServiceRegistry.() -> Unit): LogQueryService =
            LogQueryServiceRegistry().apply(block).build()
    }
}

