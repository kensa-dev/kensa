package dev.kensa.junit

import dev.kensa.*
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.DeprecationLevel.WARNING

@ExtendWith(KensaExtension::class)
interface KensaTest : WithFixturesAndOutputs {

    fun given(action: Action<GivensContext>) {
        testContext().given(action)
    }

    fun given(steps: SetupSteps) {
        testContext().given(steps)
    }

    fun given(step: SetupStep) {
        given(SetupSteps(step))
    }

    @Deprecated("use given(Action) instead", ReplaceWith("given(action)"), WARNING)
    fun given(builder: GivensBuilder) {
        testContext().given(builder)
    }

    @Deprecated("use and(Action) instead", ReplaceWith("and(action)"), WARNING)
    fun and(builder: GivensBuilder) {
        given(builder)
    }

    fun and(steps: SetupSteps) {
        testContext().given(steps)
    }

    fun and(step: SetupStep) {
        given(SetupSteps(step))
    }

    fun and(action: Action<GivensContext>) {
        given(action)
    }

    fun `when`(action: Action<ActionContext>) = whenever(action)
    fun whenever(action: Action<ActionContext>) = testContext().whenever(action)

    @Deprecated("use when(Action) instead", ReplaceWith("when(action)"), WARNING)
    fun `when`(action: ActionUnderTest) = whenever(action)

    @Deprecated("use when(Action) instead", ReplaceWith("when(action)"), WARNING)
    fun whenever(action: ActionUnderTest) {
        testContext().whenever(action)
    }

    fun disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup()
    }

    override val fixturesAndOutputs: FixturesAndOutputs get() = testContext().fixturesAndOutputs

    override val fixtures: Fixtures get() = testContext().fixtures

    override val outputs: CapturedOutputs get() = testContext().outputs
}