package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.NumberLiteralEvent
import dev.kensa.parse.Event.LiteralEvent.StringLiteralEvent
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.KotlinParserBaseListener
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinFunctionBodyParser(private val stateMachine: ParserStateMachine) : KotlinParserBaseListener() {

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        stateMachine.transition(EnterTestMethod(ctx))
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        stateMachine.transition(ExitTestMethod(ctx))
    }

    override fun enterStatement(ctx: KotlinParser.StatementContext) {
        stateMachine.transition(EnterStatementEvent(ctx))
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
        stateMachine.transition(ExitStatementEvent(ctx))
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
        stateMachine.transition(EnterMethodInvocationEvent(ctx))
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext) {
        stateMachine.transition(ExitMethodInvocationEvent(ctx))
    }

    override fun visitTerminal(node: TerminalNode) {
        /**
         * Kotlin literal assignments:
         *
         * RealLiteral=134
         * FloatLiteral=135
         * DoubleLiteral=136
         * IntegerLiteral=137
         * HexLiteral=138
         * BinLiteral=139
         * UnsignedLiteral=140
         * LongLiteral=141
         * BooleanLiteral=142
         * NullLiteral=143
         * CharacterLiteral=144
         * LineStrRef=158
         * LineStrText=159
         * LineStrEscapedChar=160
         * LineStrExprStart=161
         *
         */
        when (node.symbol.type) {
            159 -> stateMachine.transition(StringLiteralEvent(node, node.text))
            134, 135, 136, 137, 140, 141 -> stateMachine.transition(NumberLiteralEvent(node, node.text))
            145 -> stateMachine.transition(IdentifierEvent(node))

            else -> stateMachine.transition(TerminalNodeEvent(node))
        }
    }
}