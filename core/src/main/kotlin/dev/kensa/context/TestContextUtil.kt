package dev.kensa.context

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.ActionUnderTest
import dev.kensa.StateCollector
import dev.kensa.StateExtractor

object TestContextUtil {
    fun withTestContext(block: TestContextRunner.() -> Unit) {
        TestContextRunner(TestContextHolder.testContext()).apply(block)
    }
}

class TestContextRunner(private val testContext: TestContext) {
    fun execute(action: ActionUnderTest) {
        with(testContext) { action.execute(givens, interactions) }
    }

    fun execute(action: Action<ActionContext>) {
        action.execute(testContext.actionContext)
    }

    fun <T> execute(extractor: StateExtractor<T>) = testContext.run { extractor.execute(interactions) }
    fun <T> execute(extractor: StateCollector<T>) = testContext.run { extractor.execute(collectorContext) }
}