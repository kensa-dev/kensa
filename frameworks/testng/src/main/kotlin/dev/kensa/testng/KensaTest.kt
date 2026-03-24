package dev.kensa.testng

import dev.kensa.*
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.outputs.CapturedOutputs

// KensaTestNgListener is auto-discovered via ServiceLoader (META-INF/services/org.testng.ITestNGListener).
// Alternatively, add @Listeners(KensaTestNgListener::class) to your test class or abstract base class.
interface KensaTest : WithFixturesAndOutputs {

    fun given(action: Action<GivensContext>) { testContext().given(action) }
    fun given(steps: SetupSteps) { testContext().given(steps) }
    fun given(step: SetupStep) { given(SetupSteps(step)) }

    fun and(action: Action<GivensContext>) { given(action) }
    fun and(steps: SetupSteps) { testContext().given(steps) }
    fun and(step: SetupStep) { given(SetupSteps(step)) }

    fun `when`(action: Action<ActionContext>) = whenever(action)
    fun whenever(action: Action<ActionContext>) = testContext().whenever(action)

    fun disableInteractionTestGroup() { testContext().disableInteractionTestGroup() }

    override val fixturesAndOutputs: FixturesAndOutputs get() = testContext().fixturesAndOutputs
    override val fixtures get() = testContext().fixtures
    override val outputs: CapturedOutputs get() = testContext().outputs
}
