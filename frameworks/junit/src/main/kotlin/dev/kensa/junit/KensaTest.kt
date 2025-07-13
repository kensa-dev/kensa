package dev.kensa.junit

import dev.kensa.*
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.Fixtures
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(KensaExtension::class)
interface KensaTest : WithFixtures {

    fun disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup()
    }

    fun given(steps: SetupSteps) {
        steps.execute()
    }

    fun given(step: SetupStep) {
        SetupSteps(step).execute()
    }

    fun given(builder: GivensBuilder) {
        testContext().given(builder)
    }

    fun and(builder: GivensBuilder) {
        given(builder)
    }

    fun given(builder: GivensBuilderWithFixtures) {
        testContext().given(builder)
    }

    fun and(builder: GivensBuilderWithFixtures) {
        given(builder)
    }

    fun and(steps: SetupSteps) {
        steps.execute()
    }

    fun and(step: SetupStep) {
        SetupSteps(step).execute()
    }

    fun `when`(action: ActionUnderTest) = whenever(action)

    fun whenever(action: ActionUnderTest) {
        testContext().whenever(action)
    }

    fun `when`(action: ActionUnderTestWithFixtures) = whenever(action)

    fun whenever(action: ActionUnderTestWithFixtures) {
        testContext().whenever(action)
    }

    override val fixtures: Fixtures get() = testContext().fixtures

    operator fun <T> get(key: Fixture<T>): T = fixtures[key]
}