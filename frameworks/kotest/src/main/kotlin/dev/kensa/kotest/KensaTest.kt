package dev.kensa.kotest

import dev.kensa.*
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import io.kotest.core.spec.style.AnnotationSpec

/**
 * Base class for Kensa tests written with Kotest's [AnnotationSpec] style.
 *
 * Extend this class instead of [AnnotationSpec] directly.  Kensa lifecycle
 * management is handled automatically by [KensaKotestListener] (registered
 * via ServiceLoader).
 */
abstract class KensaTest : AnnotationSpec(), WithFixturesAndOutputs {

    fun given(action: Action<GivensContext>) = testContext().given(action)

    fun given(steps: SetupSteps) = testContext().given(steps)

    fun given(step: SetupStep) = given(SetupSteps(step))

    fun and(action: Action<GivensContext>) = given(action)

    fun and(steps: SetupSteps) = testContext().given(steps)

    fun and(step: SetupStep) = given(SetupSteps(step))

    fun `when`(action: Action<ActionContext>) = whenever(action)

    fun whenever(action: Action<ActionContext>) = testContext().whenever(action)

    fun disableInteractionTestGroup() = testContext().disableInteractionTestGroup()

    override val fixturesAndOutputs: FixturesAndOutputs get() = testContext().fixturesAndOutputs

    override val fixtures: Fixtures get() = testContext().fixtures

    override val outputs: CapturedOutputs get() = testContext().outputs
}
