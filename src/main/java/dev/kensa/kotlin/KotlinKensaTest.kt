package dev.kensa.kotlin

import dev.kensa.*
import dev.kensa.junit.KensaExtension
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.state.SetupStrategy
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(KensaExtension::class)
interface KotlinKensaTest {
    fun disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup()
    }

    fun setSetupStrategy(strategy: SetupStrategy) {
        testContext().setSetupStrategy(strategy)
    }

    fun given(steps: SetupSteps) {
        steps.execute()
    }

    fun given(builder: GivensBuilder) {
        testContext().given(builder)
    }

    fun and(builder: GivensBuilder) {
        given(builder)
    }

    fun and(steps: SetupSteps) {
        steps.execute()
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