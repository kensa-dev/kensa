package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.ActionUnderTestWithFixtures
import dev.kensa.StateExtractor

object TestContextUtil {
    fun withTestContext(block: TestContextRunner.() -> Unit) {
        TestContextRunner(TestContextHolder.testContext()).apply(block)
    }
}

class TestContextRunner(private val testContext: TestContext) {
    fun execute(action: ActionUnderTestWithFixtures) {
        with(testContext) { action.execute(givens, fixtures, interactions) }
    }

    fun execute(action: ActionUnderTest) {
        with(testContext) { action.execute(givens, interactions) }
    }

    fun <T> execute(extractor: StateExtractor<T>) = testContext.run { extractor.execute(interactions) }
}