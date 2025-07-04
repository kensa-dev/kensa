package dev.kensa.context

import dev.kensa.*
import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContext(val givens: Givens, val interactions: CapturedInteractions, override val fixtures: Fixtures): WithFixtures {

    fun given(builder: GivensBuilder) {
        builder.build(givens)
    }

    fun given(builder: GivensBuilderWithFixtures) {
        builder.build(givens, fixtures)
    }

    fun whenever(action: ActionUnderTest) {
        interactions.isUnderTest = true
        action.execute(givens, interactions)
    }

    fun whenever(action: ActionUnderTestWithFixtures) {
        interactions.isUnderTest = true
        action.execute(givens, fixtures, interactions)
    }

    fun disableInteractionTestGroup() {
        interactions.isUnderTestEnabled = false
    }

    companion object {
        operator fun invoke(testClass: Class<*>, testMethod: Method, setupStrategy: SetupStrategy, fixtures: Fixtures) = TestContext(Givens(), CapturedInteractions(testMethod.setupStrategy(testClass.setupStrategy(setupStrategy))), fixtures)

        private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
    }
}