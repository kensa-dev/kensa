package dev.kensa

@FunctionalInterface
fun interface StateCollector<T> {
    fun execute(context: CollectorContext): T
}