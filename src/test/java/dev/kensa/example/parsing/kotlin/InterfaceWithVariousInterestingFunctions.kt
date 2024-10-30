package dev.kensa.example.parsing.kotlin

import dev.kensa.Emphasise
import dev.kensa.NestedSentence
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

interface InterfaceWithVariousInterestingFunctions {

    @NestedSentence
    fun function1() {
        1 shouldBe 1
    }

    @NestedSentence
    fun function2() {}

    @Emphasise
    fun emphasise1() {}

    @Emphasise
    fun emphasise2() {}

    @Test
    fun test1() {}
}
