package dev.kensa.parse

import dev.kensa.sentence.TemplateSentence

sealed interface State {

    data object Start : State
    data object InMethod : State
    data class TestBlock(val parentState: State) : State
    data class ExpressionFn(val parentState: State) : State

    data class InStatement(val parentState: State, val didBegin: Boolean = true) : State
    data class InMethodInvocation(val parentState: State, val didBegin: Boolean = false) : State
    data class InExpression(val parentState: State) : State
    data class InRenderedValueExpression(val parentState: State) : State
    data class InLambda(val parentState: State) : State
    data class InTypeArguments(val parentState: State) : State
    data class InFixturesExpression(val parentState: State) : State
    data class InOutputsExpression(val parentState: State) : State
    data class InChainedCallExpression(val parentState: State) : State

    sealed interface WithAppendable : State {
        fun append(event: LocatedEvent)

        data class InExpandableWithArgumentsParameter(val parentState: WithAppendable) : WithAppendable {
            override fun append(event: LocatedEvent) {
                parentState.append(event)
            }
        }

        data class InExpandableWithArguments(val parentState: State, val location: Location, val name: String, val sentences: List<TemplateSentence>, val _parameterEvents: MutableList<LocatedEvent> = mutableListOf()) : WithAppendable {
            val parameterEvents = _parameterEvents
            override fun append(event: LocatedEvent) {
                _parameterEvents.add(event)
            }
        }
    }

    data object End : State
}