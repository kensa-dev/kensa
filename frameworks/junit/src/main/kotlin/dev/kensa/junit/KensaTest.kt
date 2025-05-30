package dev.kensa.junit

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.SetupSteps
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.Fixtures
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(KensaExtension::class)
interface KensaTest {

    fun disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup()
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

    fun whenever(action: ActionUnderTest) {
        testContext().whenever(action)
    }

    val fixtures: Fixtures get() = testContext().fixtures

    operator fun <T> get(key: Fixture<T>): T = fixtures[key]

    fun <T> fixtures(key: Fixture<T>) : T = testContext().fixture(key)
}