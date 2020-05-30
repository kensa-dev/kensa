package dev.kensa.kotlin

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.GivensWithInteractionsBuilder
import dev.kensa.KensaExtension
import dev.kensa.context.TestContextHolder.testContext
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(KensaExtension::class)
interface KotlinKensaTest {
    fun disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup()
    }

    fun given(builder: GivensBuilder) {
        testContext().given(builder)
    }

    fun and(builder: GivensBuilder) {
        given(builder)
    }

    fun given(builder: GivensWithInteractionsBuilder) {
        testContext().given(builder)
    }

    fun and(builder: GivensWithInteractionsBuilder) {
        given(builder)
    }

    fun whenever(action: ActionUnderTest) {
        testContext().whenever(action)
    }
}