package dev.kensa.parse.kotlin

import dev.kensa.parse.Event.*
import dev.kensa.parse.kotlin.KotlinLexer.*
import dev.kensa.parse.kotlin.KotlinParser.ValueArgumentContext
import dev.kensa.parse.Location
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
import dev.kensa.parse.ParseContext.Companion.asReplaceSentenceHint
import dev.kensa.parse.ParseContext.Companion.asStringLiteral
import dev.kensa.parse.ParserStateMachine
import dev.kensa.parse.ReplaceSentenceHintParser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

class KotlinFunctionBodyParser(
    private val stateMachine: ParserStateMachine,
    private val parseContext: ParseContext,
    private val hintParser: ReplaceSentenceHintParser,
) : KotlinParserBaseListener() {

    private var replacedStatementDepth = 0

    override fun enterEveryRule(ctx: ParserRuleContext) {}
    override fun exitEveryRule(ctx: ParserRuleContext) {}

    override fun enterLambdaLiteral(ctx: KotlinParser.LambdaLiteralContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterLambda)
    }

    override fun exitLambdaLiteral(ctx: KotlinParser.LambdaLiteralContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitLambda)
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext) {
        if (replacedStatementDepth > 0) return
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
        if (replacedStatementDepth > 0) return
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
        if (replacedStatementDepth > 0) return
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
        if (replacedStatementDepth > 0) return
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
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterMethod)
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitMethod)
    }

    override fun enterBlock(ctx: KotlinParser.BlockContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterBlock)
    }

    override fun exitBlock(ctx: KotlinParser.BlockContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitBlock)
    }

    override fun enterStatement(ctx: KotlinParser.StatementContext) {
        if (replacedStatementDepth > 0) {
            replacedStatementDepth++
            return
        }
        ctx.asNote()?.also { stateMachine.apply(it) }
        stateMachine.apply(ctx.asEnterStatement())
        ctx.asReplaceSentenceHint()?.also { hint ->
            hintParser.emitEvents(hint, Location(ctx.start.line, ctx.start.charPositionInLine), stateMachine)
            replacedStatementDepth = 1
        }
    }

    override fun exitStatement(ctx: KotlinParser.StatementContext) {
        if (replacedStatementDepth > 0) {
            replacedStatementDepth--
            if (replacedStatementDepth == 0) stateMachine.apply(ExitStatement)
            return
        }
        stateMachine.apply(ExitStatement)
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext) {
        if (replacedStatementDepth > 0) return
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
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitExpression)
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext) {
        if (replacedStatementDepth > 0) return
        with(parseContext) {
            stateMachine.apply(
                ctx.asParameter()
                    ?: ctx.asField()
                    ?: ctx.asMethod()
                    ?: ctx.asExpandableSentence()?.let { expandable -> if (ctx.hasArguments()) expandable.asExpandableSentenceWithArguments() else expandable }
                    ?: ctx.asExpandableValue()?.let { expandable -> if (ctx.hasArguments()) expandable.asExpandableValueWithArguments() else expandable }
                    ?: ctx.asIdentifier()
            )
        }
    }

    override fun enterValueArgument(ctx: ValueArgumentContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterValueArgument)
    }

    override fun exitValueArgument(ctx: ValueArgumentContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitValueArgument)
    }

    override fun enterValueArguments(ctx: KotlinParser.ValueArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterValueArguments)
    }

    override fun exitValueArguments(ctx: KotlinParser.ValueArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitValueArguments)
    }

    override fun enterTypeArguments(ctx: KotlinParser.TypeArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterTypeArguments)
    }

    override fun exitTypeArguments(ctx: KotlinParser.TypeArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitTypeArguments)
    }

    override fun visitTerminal(node: TerminalNode) {
        if (replacedStatementDepth > 0) return
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
