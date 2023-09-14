package dev.kensa.parse

import org.antlr.v4.runtime.tree.ParseTree

sealed class State {
    object Start : State()

    class InTestMethod(val parseTree: ParseTree, val isExpressionFunction: Boolean) : State()
    class InStatement(val parseTree: ParseTree, val parentState: InTestMethod) : State()
    class InMethodCall(val parseTree: ParseTree, val parentState: State, val didBegin: Boolean = false) : State()
    class InScenarioCall(val parseTree: ParseTree, val parentState: State, val scenarioName: String) : State()

    object End : State()
}