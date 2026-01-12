package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.kotlin.KotlinLexer.*
import dev.kensa.parse.kotlin.KotlinParser.ValueArgumentContext
import dev.kensa.parse.ParseContext
import dev.kensa.parse.ParseContext.Companion.asBooleanLiteral
import dev.kensa.parse.ParseContext.Companion.asCharacterLiteral
import dev.kensa.parse.ParseContext.Companion.asNote
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

    // For Debugging:
    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println(">Exiting: ${ctx::class.simpleName} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }


    override fun enterLambdaLiteral(ctx: KotlinParser.LambdaLiteralContext) {
        stateMachine.apply(EnterLambda)
    }

    override fun exitLambdaLiteral(ctx: KotlinParser.LambdaLiteralContext) {
        stateMachine.apply(ExitLambda)
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext) {
        with(parseContext) {
            val rhExpression = ctx.rangeExpression(1)
            if (rhExpression.matchesFixturesExpression()
                || rhExpression.matchesOutputsExpression()
                || rhExpression.matchesChainedCall()
            ) {
                stateMachine.apply(ctx.asEnterExpression())
            }
        }
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext) {
        with(parseContext) {
            val rhExpression = ctx.rangeExpression(1)
            if (rhExpression.matchesFixturesExpression()
                || rhExpression.matchesOutputsExpression()
                || rhExpression.matchesChainedCall()
            ) {
                stateMachine.apply(ExitExpression)
            }
        }
    }

    override fun enterRangeExpression(ctx: KotlinParser.RangeExpressionContext) {
        with(parseContext) {
            when {
                ctx.matchesFixturesExpression() -> ctx.asFixture()
                ctx.matchesOutputsExpression() -> ctx.asOutputs()
                ctx.matchesRenderedValueMethodExpression() -> ctx.asRenderedValueMethodExpression()
                ctx.matchesChainedCall() -> ctx.asChainedCall()
                else -> null
            }?.also { stateMachine.apply(it) }
        }
    }

    override fun exitRangeExpression(ctx: KotlinParser.RangeExpressionContext) {
        with(parseContext) {
            if (ctx.matchesFixturesExpression()
                || ctx.matchesOutputsExpression()
                || ctx.matchesRenderedValueMethodExpression()
                || ctx.matchesChainedCall()
            ) {
                stateMachine.apply(ExitExpression)
            }
        }
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
        ctx.asNote()?.also {
            stateMachine.apply(it)
        }

        stateMachine.apply(ctx.asEnterStatement())
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
        stateMachine.apply(ExitStatement)
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
        return with(parseContext) {
            when {
                ctx.matchesFixturesExpression() -> ctx.asFixture()
                ctx.matchesOutputsExpression() -> ctx.asOutputs()
                ctx.matchesRenderedValueMethodExpression() -> ctx.asRenderedValueMethodExpression()
                ctx.matchesChainedCall() -> ctx.asChainedCall()
                else -> ctx.asEnterExpression()
            }?.also { stateMachine.apply(it) }
        }
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext) {
        stateMachine.apply(ExitExpression)
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext) {
        with(parseContext) {
            stateMachine.apply(ctx.asParameter() ?: ctx.asField() ?: ctx.asMethod() ?: ctx.asNested()?.let { nested ->
                if (ctx.hasArguments())
                    nested.asNestedWithArguments()
                else
                    nested
            } ?: ctx.asIdentifier())
        }
    }

    override fun enterValueArgument(ctx: ValueArgumentContext) {
        stateMachine.apply(EnterValueArgument)
    }

    override fun exitValueArgument(ctx: ValueArgumentContext) {
        stateMachine.apply(ExitValueArgument)
    }

    override fun enterValueArguments(ctx: KotlinParser.ValueArgumentsContext) {
        stateMachine.apply(EnterValueArguments)
    }

    override fun exitValueArguments(ctx: KotlinParser.ValueArgumentsContext) {
        stateMachine.apply(ExitValueArguments)
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
                    RPAREN, RCURL -> node.asNote()?.also { stateMachine.apply(it) }
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

    private fun ParserRuleContext.hasArguments(): Boolean {
        fun ParserRuleContext.findValueArguments(): Boolean {
            return children?.any { child ->
                when (child) {
                    is ValueArgumentContext -> true
                    is ParserRuleContext -> child.findValueArguments()
                    else -> false
                }
            } ?: false
        }

        return (parent?.parent as? ParserRuleContext)?.findValueArguments() ?: false
    }
}
