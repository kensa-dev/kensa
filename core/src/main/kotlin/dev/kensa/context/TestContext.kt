package dev.kensa.context

import dev.kensa.*
import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.DeprecationLevel.WARNING

class TestContext(val givens: Givens, val interactions: CapturedInteractions, override val fixtures: Fixtures, override val outputs: CapturedOutputs) : WithFixturesAndOutputs {

    val collectorContext = CollectorContext(fixtures, interactions, outputs)
    override val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
    private val givensContext = GivensContext(fixtures)
    private val actionContext = ActionContext(fixtures, interactions, outputs)

    fun given(action: Action<GivensContext>) {
        action.execute(givensContext)
    }

    fun given(steps: SetupSteps) {
        val givensContext = givensContext
        val actionContext = actionContext

        steps.forEach { step ->
            step.givens().buildWith(givensContext).executeWith(givens, givensContext)
            step.actions().buildWith(actionContext).executeWith(givens, actionContext)
            step.verify().verifyWith(collectorContext)
        }
    }

    @Deprecated("use given(Action) instead", ReplaceWith("given(action)"), WARNING)
    fun given(builder: GivensBuilder) {
        builder.build(givens)
    }

    fun whenever(action: Action<ActionContext>) {
        interactions.isUnderTest = true
        action.execute(actionContext)
    }

    @Deprecated("use whenever(Action) instead", ReplaceWith("whenever(action)"), WARNING)
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
        operator fun invoke(testClass: Class<*>, testMethod: Method, setupStrategy: SetupStrategy) =
            TestContext(Givens(), CapturedInteractions(testMethod.setupStrategy(testClass.setupStrategy(setupStrategy))), Fixtures(), CapturedOutputs())

        private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
    }
}