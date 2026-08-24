package dev.kensa.context

import dev.kensa.Action
import dev.kensa.KensaInternalApi
import dev.kensa.ActionContext
import dev.kensa.StateCollector

object TestContextUtil {
    @KensaInternalApi
    @Deprecated("Use SetupStep instead - implement dev.kensa.SetupStep and register it with the test's setup steps.")
    fun withTestContext(block: TestContextRunner.() -> Unit) {
        TestContextRunner(TestContextHolder.testContext()).apply(block)
    }
}

@KensaInternalApi
@Deprecated("Use SetupStep instead - implement dev.kensa.SetupStep and register it with the test's setup steps.")
class TestContextRunner(private val testContext: TestContext) {
    fun execute(action: Action<ActionContext>) {
        action.execute(testContext.actionContext)
    }

    fun <T> execute(collector: StateCollector<T>) = testContext.run { collector.execute(collectorContext) }
}