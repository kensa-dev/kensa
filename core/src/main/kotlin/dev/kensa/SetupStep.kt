package dev.kensa

import dev.kensa.ActionsUnderTest.Companion.buildActions
import dev.kensa.GivensBuilders.Companion.buildGivens
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixtures
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens

interface SetupStep {
    fun givens(): GivensBuilders = buildGivens()
    fun actions(): ActionsUnderTest = buildActions()
    fun verify(): Verification = Verification.Companion.verify()
}

class GivensHolder {

    val list = mutableListOf<Any>()

    fun add(givens: GivensBuilder) {
        list.add(givens)
    }

    fun add(givens: GivensBuilderWithFixtures) {
        list.add(givens)
    }

    fun executeWith(givens: Givens, fixtures: Fixtures) {
        list.forEach {
            when (it) {
                is GivensBuilder -> it.build(givens)
                is GivensBuilderWithFixtures -> it.build(givens, fixtures)
                else -> throw IllegalStateException("Unexpected type in GivensHolder")
            }
        }
    }
}

class ActionUnderTestHolder {

    val list = mutableListOf<Any>()

    fun add(actionUnderTest: ActionUnderTest) {
        list.add(actionUnderTest)
    }

    fun add(actionUnderTest: ActionUnderTestWithFixtures) {
        list.add(actionUnderTest)
    }

    fun executeWith(givens: Givens, fixtures: Fixtures, interactions: CapturedInteractions) {
        list.forEach {
            when (it) {
                is ActionUnderTest -> it.execute(givens, interactions)
                is ActionUnderTestWithFixtures -> it.execute(givens, fixtures, interactions)
                else -> throw IllegalStateException("Unexpected type in ActionUnderTestHolder")
            }
        }
    }
}

class GivensBuilders private constructor(private val block1: GivensHolder.(Fixtures) -> Unit) {

    fun buildWith(fixtures: Fixtures): GivensHolder = GivensHolder().apply { block1(fixtures) }

    companion object {
        fun buildGivens(block: GivensHolder.(Fixtures) -> Unit = { }) = GivensBuilders(block)
    }
}

class ActionsUnderTest private constructor(private val block: ActionUnderTestHolder.(Fixtures) -> Unit) {

    fun buildWith(fixtures: Fixtures): ActionUnderTestHolder = ActionUnderTestHolder().apply { block(fixtures) }

    companion object {
        fun buildActions(block: ActionUnderTestHolder.(Fixtures) -> Unit = { }) = ActionsUnderTest(block)
    }
}

class Verification private constructor(private val block: (Fixtures) -> Unit) {

    fun verifyWith(fixtures: Fixtures) = block(fixtures)

    companion object {
        fun verify(block: (Fixtures) -> Unit = { }) = Verification(block)
    }
}

class SetupSteps(vararg steps: SetupStep) {

    private val steps = steps.toList()

    fun execute() {
        with(testContext()) {
            steps.forEach { step ->
                step.givens().buildWith(fixtures).executeWith(givens, fixtures)
                step.actions().buildWith(fixtures).executeWith(givens, fixtures, interactions)
                step.verify().verifyWith(fixtures)
            }
        }
    }
}