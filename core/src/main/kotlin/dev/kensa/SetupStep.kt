package dev.kensa

import dev.kensa.ActionBlockBuilder.Companion.buildActions
import dev.kensa.GivensBlockBuilder.Companion.buildGivens
import dev.kensa.state.Givens

interface SetupStep {
    fun givens(): GivensBlockBuilder = buildGivens()
    fun actions(): ActionBlockBuilder = buildActions()
    fun verify(): VerificationBlockBuilder = VerificationBlockBuilder.Companion.verify()
}

fun SetupStep.and(other: SetupStep): SetupSteps = SetupSteps(this).apply { add(other) }
fun SetupSteps.and(step: SetupStep): SetupSteps = apply { add(step) }

class GivensHolder {

    val list = mutableListOf<Any>()

    fun add(givens: GivensBuilder) {
        list.add(givens)
    }

    fun add(action: Action<GivensContext>) {
        list.add(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun executeWith(givens: Givens, context: GivensContext) {
        list.forEach {
            when (it) {
                is GivensBuilder -> it.build(givens)
                is Action<*> -> (it as Action<GivensContext>).execute(context)
                else -> throw IllegalStateException("Unexpected type in GivensHolder")
            }
        }
    }
}

class ActionsHolder {

    val list = mutableListOf<Any>()

    fun add(actionUnderTest: ActionUnderTest) {
        list.add(actionUnderTest)
    }

    fun add(action: Action<ActionContext>) {
        list.add(action)
    }

    @Suppress("UNCHECKED_CAST")
    fun executeWith(givens: Givens, context: ActionContext) {
        list.forEach {
            when (it) {
                is ActionUnderTest -> it.execute(givens, context.interactions)
                is Action<*> -> (it as Action<ActionContext>).execute(context)
                else -> throw IllegalStateException("Unexpected type in ActionUnderTestHolder")
            }
        }
    }
}

class GivensBlockBuilder private constructor(private val block: GivensHolder.(GivensContext) -> Unit) {

    fun buildWith(context: GivensContext): GivensHolder = GivensHolder().apply { block(context) }

    companion object {
        fun buildGivens(block: GivensHolder.(GivensContext) -> Unit = { }) = GivensBlockBuilder(block)
    }
}

class ActionBlockBuilder private constructor(private val block: ActionsHolder.(ActionContext) -> Unit) {

    fun buildWith(context: ActionContext): ActionsHolder = ActionsHolder().apply { block(context) }

    companion object {
        fun buildActions(block: ActionsHolder.(ActionContext) -> Unit = { }) = ActionBlockBuilder(block)
    }
}

class VerificationBlockBuilder private constructor(private val block: (CollectorContext) -> Unit) {

    fun verifyWith(context: CollectorContext) = block(context)

    companion object {
        fun verify(block: (CollectorContext) -> Unit = {}) = VerificationBlockBuilder(block)
    }
}

class SetupSteps(vararg steps: SetupStep) : Sequence<SetupStep> {

    private val steps = mutableListOf(*steps)

    internal fun add(step: SetupStep) {
        steps.add(step)
    }

    override fun iterator(): Iterator<SetupStep> = steps.iterator()
}