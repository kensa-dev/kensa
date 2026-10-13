package dev.kensa

@FunctionalInterface
fun interface StateCollector<T> {
    fun execute(context: CollectorContext): T

    companion object {
        /**
         * Adapts a [ThrowingCollector] to a [StateCollector], so a Java lambda can throw checked exceptions.
         * The exception propagates unchanged.
         */
        @JvmStatic
        fun <T> of(collector: ThrowingCollector<T>): StateCollector<T> = StateCollector { collector.execute(it) }
    }
}

/**
 * A [StateCollector] body that may throw checked exceptions, for Java callers. Adapt it with [StateCollector.of].
 */
@FunctionalInterface
fun interface ThrowingCollector<T> {
    @Throws(Exception::class)
    fun execute(context: CollectorContext): T
}
