package dev.kensa.context

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.StateCollector

object TestContextUtil {
    fun withTestContext(block: TestContextRunner.() -> Unit) {
        TestContextRunner(TestContextHolder.testContext()).apply(block)
    }
}

class TestContextRunner(private val testContext: TestContext) {
    fun execute(action: Action<ActionContext>) {
        action.execute(testContext.actionContext)
    }

    fun <T> execute(collector: StateCollector<T>) = testContext.run { collector.execute(collectorContext) }
}