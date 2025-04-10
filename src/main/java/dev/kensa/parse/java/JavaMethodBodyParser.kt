package dev.kensa.parse.java

import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.NumberLiteralEvent
import dev.kensa.parse.Event.LiteralEvent.StringLiteralEvent
import dev.kensa.parse.Java20Lexer.*
import dev.kensa.parse.Java20Parser
import dev.kensa.parse.Java20Parser.TextBlock
import dev.kensa.parse.Java20ParserBaseListener
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.tree.TerminalNode

class JavaMethodBodyParser(private val stateMachine: ParserStateMachine) : Java20ParserBaseListener() {

    //  For Debugging:
//    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
//    }
//
//    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
//    }

    override fun enterExpression(ctx: Java20Parser.ExpressionContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterExpressionEvent(ctx))
    }

    override fun exitExpression(ctx: Java20Parser.ExpressionContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitExpressionEvent(ctx))
    }

    override fun enterMethodBody(ctx: Java20Parser.MethodBodyContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterMethodEvent(ctx))
    }

    override fun exitMethodBody(ctx: Java20Parser.MethodBodyContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitMethodEvent(ctx))
    }

    override fun enterStatement(ctx: Java20Parser.StatementContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterStatementEvent(ctx))
    }

    override fun exitStatement(ctx: Java20Parser.StatementContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitStatementEvent(ctx))
    }

    override fun enterMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun enterTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        stateMachine.transition(EnterTypeArgumentsEvent(ctx))
    }

    override fun exitTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        stateMachine.transition(ExitTypeArgumentsEvent(ctx))
    }

    override fun visitTerminal(node: TerminalNode) {
//        println("Terminal: ${node.symbol.type} :: ${node.text}")
        when (node.symbol.type) {
            IntegerLiteral, FloatingPointLiteral -> stateMachine.transition(NumberLiteralEvent(node))
            CharacterLiteral, StringLiteral -> stateMachine.transition(StringLiteralEvent(node, node.text.extractGroup(1, optionalQuotesRegex)))
            TextBlock -> stateMachine.transition(LiteralEvent.MultiLineStringEvent(node))
            Identifier -> stateMachine.transition(IdentifierEvent(node))

            else -> stateMachine.transition(TerminalNodeEvent(node))
        }
    }

    companion object {
        private val optionalQuotesRegex = "^\"(.*)\"$|^(.*)$".toRegex()

        private fun String.extractGroup(group: Int, regex: Regex) = regex.matchEntire(this)?.groupValues?.get(group) ?: this
    }
}