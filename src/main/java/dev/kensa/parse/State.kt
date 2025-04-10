package dev.kensa.parse

import org.antlr.v4.runtime.tree.ParseTree

sealed class State {

    data object Start : State()

    sealed class ParseTreeState(val parseTree: ParseTree) : State() {
        class InMethod(parseTree: ParseTree, val isExpressionFunction: Boolean) : ParseTreeState(parseTree)
        class InStatement(parseTree: ParseTree, val parentState: InMethod) : ParseTreeState(parseTree)
        class InExpression(parseTree: ParseTree, val parentState: State) : ParseTreeState(parseTree)
        class InMethodCall(parseTree: ParseTree, val parentState: State, val didBegin: Boolean = false) : ParseTreeState(parseTree)
        class InTypeArguments(parseTree: ParseTree, val parentState: State) : ParseTreeState(parseTree)
        class InScenarioCall(parseTree: ParseTree, val parentState: State, val scenarioName: String) : ParseTreeState(parseTree)

        override fun toString(): String = "${this::class.java.simpleName} :: ${parseTree.text}"
    }

    data object End : State()
}