package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.KotlinLexer.*
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.KotlinParserBaseListener
import dev.kensa.parse.ParseContext
import dev.kensa.parse.ParseContext.Companion.asBooleanLiteral
import dev.kensa.parse.ParseContext.Companion.asCharacterLiteral
import dev.kensa.parse.ParseContext.Companion.asEnterExpression
import dev.kensa.parse.ParseContext.Companion.asEnterStatement
import dev.kensa.parse.ParseContext.Companion.asMultilineString
import dev.kensa.parse.ParseContext.Companion.asNullLiteral
import dev.kensa.parse.ParseContext.Companion.asNumberLiteral
import dev.kensa.parse.ParseContext.Companion.asOperator
import dev.kensa.parse.ParseContext.Companion.asStringLiteral
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinFunctionBodyParser(
    private val stateMachine: ParserStateMachine,
    private val parseContext: ParseContext
) : KotlinParserBaseListener() {

    //  For Debugging:
    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println(">Entering: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println(">Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        stateMachine.apply(EnterMethod)
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        stateMachine.apply(ExitMethod)
    }

    override fun enterBlock(ctx: KotlinParser.BlockContext) {
        stateMachine.apply(EnterBlock)
    }

    override fun exitBlock(ctx: KotlinParser.BlockContext) {
        stateMachine.apply(ExitBlock)
    }

    override fun enterStatement(ctx: KotlinParser.StatementContext) {
        stateMachine.apply(ctx.asEnterStatement())
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
        stateMachine.apply(ExitStatement)
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) =
        with(parseContext) {
            stateMachine.apply(ctx.asScenario() ?: ctx.asEnterExpression())
        }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext) {
        stateMachine.apply(ExitExpression)
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext) =
        with(parseContext) {
            stateMachine.apply(ctx.asParameter() ?: ctx.asField() ?: ctx.asMethod() ?: ctx.asNested() ?: ctx.asIdentifier())
        }

    override fun enterTypeArguments(ctx: KotlinParser.TypeArgumentsContext) {
        stateMachine.apply(EnterTypeArguments)
    }

    override fun exitTypeArguments(ctx: KotlinParser.TypeArgumentsContext) {
        stateMachine.apply(ExitTypeArguments)
    }

    override fun visitTerminal(node: TerminalNode) {
        with(parseContext) {
            with(node) {
                when (symbol.type) {
                    ASSIGNMENT, ARROW -> stateMachine.apply(asOperator())
                    BooleanLiteral -> stateMachine.apply(asBooleanLiteral())
                    CharacterLiteral -> stateMachine.apply(asCharacterLiteral())
                    LineStrText -> stateMachine.apply(asStringLiteral())
                    MultiLineStrText -> stateMachine.apply(asMultilineString())
                    DoubleLiteral, FloatLiteral, HexLiteral, LongLiteral, IntegerLiteral, RealLiteral, UnsignedLiteral -> stateMachine.apply(asNumberLiteral())
                    NullLiteral -> stateMachine.apply(asNullLiteral())

                    else -> stateMachine.apply(Terminal)
                }
            }
        }
    }
}