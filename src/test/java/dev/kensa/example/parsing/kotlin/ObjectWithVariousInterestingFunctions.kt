package dev.kensa.example.parsing.kotlin

import dev.kensa.NestedSentence
import io.kotest.matchers.shouldBe

object ObjectWithVariousInterestingFunctions {
    @NestedSentence
    fun function3() {
        10 shouldBe 10
    }


}