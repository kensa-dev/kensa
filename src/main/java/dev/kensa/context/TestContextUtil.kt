package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.StateExtractor

object TestContextUtil {
    fun executeWithContext(action: ActionUnderTest) {
        TestContextHolder.testContext().apply { action.execute(givens, interactions) }
    }

    fun executeWithContext(extractor: StateExtractor<*>) {
        TestContextHolder.testContext().apply { extractor.execute(interactions) }
    }
}