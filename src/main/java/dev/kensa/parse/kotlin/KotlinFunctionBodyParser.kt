package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.*
import dev.kensa.parse.KotlinLexer.*
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.KotlinParserBaseListener
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinFunctionBodyParser(private val stateMachine: ParserStateMachine) : KotlinParserBaseListener() {

//  For Debugging:
//    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println(">Entering: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state")
//    }

//    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println(">Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state")
//    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
//        println("Entering: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(EnterMethodEvent(ctx))
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
//        println("Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(ExitMethodEvent(ctx))
    }

    override fun enterStatement(ctx: KotlinParser.StatementContext) {
//        println("Entering: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(EnterStatementEvent(ctx))
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
//        println("Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(ExitStatementEvent(ctx))
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
//        println("Entering: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext) {
//        println("Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun visitTerminal(node: TerminalNode) {
//        println("Visit Terminal: ${node.text} ${node.symbol.type}")
        when (node.symbol.type) {
            ASSIGNMENT, ARROW -> stateMachine.transition(OperatorEvent(node))
            BooleanLiteral -> stateMachine.transition(BooleanLiteralEvent(node))
            CharacterLiteral -> stateMachine.transition(CharacterLiteralEvent(node))
            LineStrText -> stateMachine.transition(StringLiteralEvent(node, node.text))
            MultiLineStrText -> stateMachine.transition(MultiLineStringEvent(node))
            DoubleLiteral, FloatLiteral, HexLiteral, LongLiteral, IntegerLiteral, RealLiteral, UnsignedLiteral -> stateMachine.transition(NumberLiteralEvent(node))
            Identifier, VALUE -> stateMachine.transition(IdentifierEvent(node))
            NullLiteral -> stateMachine.transition(NullLiteralEvent(node))

            else -> stateMachine.transition(TerminalNodeEvent(node))
        }
    }
}