package dev.kensa.kotest

import dev.kensa.CollectorContext
import dev.kensa.StateCollector
import io.kotest.matchers.Matcher
import io.kotest.matchers.invokeMatcher
import java.util.concurrent.atomic.AtomicBoolean

class PollingScope internal constructor(private val collectorContext: CollectorContext) {

    internal val checks = mutableListOf<() -> Unit>()

    fun <T> then(collector: StateCollector<T>, matcher: Matcher<T>) {
        then(collector) { invokeMatcher(this, matcher) }
    }

    fun <T> then(collector: StateCollector<T>, block: T.() -> Unit) {
        checks += { block(collector.execute(collectorContext)) }
    }

    fun <T> then(spec: ThenSpec<T>) {
        val matched = AtomicBoolean(false)
        checks += {
            val value = spec.collector.execute(collectorContext)
            invokeMatcher(value, spec.matcher)
            if (matched.compareAndSet(false, true)) {
                try {
                    spec.onMatch(collectorContext, value)
                } catch (e: Throwable) {
                    throw OnMatchException(e)
                }
            }
        }
    }

    fun <T> and(collector: StateCollector<T>, matcher: Matcher<T>): Unit = then(collector, matcher)
    fun <T> and(collector: StateCollector<T>, block: T.() -> Unit): Unit = then(collector, block)
    fun <T> and(spec: ThenSpec<T>): Unit = then(spec)
}
