package dev.kensa.parse

sealed class State {

    data object Start : State()
    data object InMethod : State()
    data class TestBlock(val parentState: State) : State()
    data class ExpressionFn(val parentState: State) : State()

    data class InStatement(val parentState: State) : State()
    data class InMethodInvocation(val parentState: State, val didBegin: Boolean = false) : State()
    data class InExpression(val parentState: State) : State()
    data class InTypeArguments(val parentState: State) : State()
    data class InScenarioExpression(val parentState: State) : State()
    data class InFixturesExpression(val parentState: State) : State()

    data object End : State()
}