package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.TestContextHolder

interface WithKotest {
    fun <T> then(extractor: StateExtractor<T>, block: T?.() -> Unit) {
        block(extractor.execute(TestContextHolder.testContext().interactions))
    }

}