package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.NumberLiteralEvent
import dev.kensa.parse.Event.LiteralEvent.StringLiteralEvent
import dev.kensa.parse.KotlinLexer.*
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.KotlinParserBaseListener
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinFunctionBodyParser(private val stateMachine: ParserStateMachine) : KotlinParserBaseListener() {

//  For Debugging:
//    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println(">Entering: ${ctx::class} :: ${ctx.text}")
//    }
//
//    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println(">Exiting: ${ctx::class} :: ${ctx.text}")
//    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterTestMethodEvent(ctx))
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitTestMethodEvent(ctx))
    }

    override fun enterStatement(ctx: KotlinParser.StatementContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterStatementEvent(ctx))
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitStatementEvent(ctx))
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text}")
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun visitTerminal(node: TerminalNode) {
//        println("Visit Terminal: ${node.text}")
        when (node.symbol.type) {
            ASSIGNMENT, ARROW -> stateMachine.transition(OperatorEvent(node, node.text))
            LineStrText -> stateMachine.transition(StringLiteralEvent(node, node.text))
            RealLiteral, FloatLiteral, DoubleLiteral, IntegerLiteral, UnsignedLiteral, LongLiteral -> stateMachine.transition(NumberLiteralEvent(node, node.text))
            Identifier, VALUE -> stateMachine.transition(IdentifierEvent(node))

            else -> stateMachine.transition(TerminalNodeEvent(node))
        }
    }
}