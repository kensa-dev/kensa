package dev.kensa

import dev.kensa.ActionsUnderTest.Companion.buildActions
import dev.kensa.GivensBuilders.Companion.buildGivens
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.Fixtures

interface SetupStep {
    fun givens(): GivensBuilders = buildGivens()
    fun actions(): ActionsUnderTest = buildActions()
    fun verify(): Verification = Verification.Companion.verify()
}

class GivensBuilders private constructor(private val block: MutableList<GivensBuilderWithFixtures>.(Fixtures) -> Unit) {

    fun buildWith(fixtures: Fixtures): List<GivensBuilderWithFixtures> = buildList { block(this, fixtures) }

    companion object {
        fun buildGivens(block: MutableList<GivensBuilderWithFixtures>.(Fixtures) -> Unit = { }) = GivensBuilders(block)
    }
}

class ActionsUnderTest private constructor(private val block: MutableList<ActionUnderTest>.(Fixtures) -> Unit) {

    fun buildWith(fixtures: Fixtures): List<ActionUnderTest> = buildList { block(this, fixtures) }

    companion object {
        fun buildActions(block: MutableList<ActionUnderTest>.(Fixtures) -> Unit = { }) = ActionsUnderTest(block)
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
                step.givens().buildWith(fixtures).forEach { given(it) }
                step.actions().buildWith(fixtures).forEach { whenever(it) }
                step.verify().verifyWith(fixtures)
            }
        }
    }
}