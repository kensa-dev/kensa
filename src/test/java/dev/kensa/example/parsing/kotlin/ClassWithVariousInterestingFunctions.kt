package dev.kensa.example.parsing.kotlin

import dev.kensa.Emphasise
import dev.kensa.NestedSentence
import org.junit.jupiter.api.Test

class ClassWithVariousInterestingFunctions {

    @NestedSentence
    fun function1() {}

    @NestedSentence
    fun function2() {}

    @Emphasise
    fun emphasise1() {}

    @Emphasise
    fun emphasise2() {}

    @Test
    fun test1() {}
}