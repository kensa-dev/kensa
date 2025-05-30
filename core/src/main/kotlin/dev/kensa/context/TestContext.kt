package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.UseSetupStrategy
import dev.kensa.fixture.Fixture
import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContext(val givens: Givens, val interactions: CapturedInteractions, val fixtures: Fixtures) {
    fun given(builder: GivensBuilder) {
        builder.build(givens, fixtures)
    }

    fun whenever(action: ActionUnderTest) {
        interactions.isUnderTest = true
        action.execute(givens, fixtures, interactions)
    }

    fun disableInteractionTestGroup() {
        interactions.isUnderTestEnabled = false
    }

    fun <T> fixture(key: Fixture<T>): T = fixtures[key]

    companion object {
        operator fun invoke(testClass: Class<*>, testMethod: Method, setupStrategy: SetupStrategy, fixtures: Fixtures) = TestContext(Givens(), CapturedInteractions(testMethod.setupStrategy(testClass.setupStrategy(setupStrategy))), fixtures)

        private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
    }
}