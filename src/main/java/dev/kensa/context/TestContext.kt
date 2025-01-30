package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.GivensWithInteractionsBuilder
import dev.kensa.Tab
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy

class TestContext(val givens: Givens, val interactions: CapturedInteractions) {
    fun given(builder: GivensWithInteractionsBuilder) {
        builder.build(givens, interactions)
    }

    fun given(builder: GivensBuilder) {
        builder.build(givens)
    }

    fun whenever(action: ActionUnderTest) {
        interactions.isUnderTest = true
        action.execute(givens, interactions)
    }

    fun disableInteractionTestGroup() {
        interactions.isUnderTestEnabled = false
    }
}