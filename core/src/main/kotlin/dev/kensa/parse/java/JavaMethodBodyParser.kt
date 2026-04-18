package dev.kensa.parse.java

import dev.kensa.parse.Event.*
import dev.kensa.parse.Location
import dev.kensa.parse.ParseContext
import dev.kensa.parse.ParseContext.Companion.asBooleanLiteral
import dev.kensa.parse.ParseContext.Companion.asCharacterLiteral
import dev.kensa.parse.ParseContext.Companion.asEnterExpression
import dev.kensa.parse.ParseContext.Companion.asEnterStatement
import dev.kensa.parse.ParseContext.Companion.asMethodInvocation
import dev.kensa.parse.ParseContext.Companion.asMultilineString
import dev.kensa.parse.ParseContext.Companion.asNote
import dev.kensa.parse.ParseContext.Companion.asNullLiteral
import dev.kensa.parse.ParseContext.Companion.asNumberLiteral
import dev.kensa.parse.ParseContext.Companion.asReplaceSentenceHint
import dev.kensa.parse.ParseContext.Companion.asStringLiteral
import dev.kensa.parse.ParserStateMachine
import dev.kensa.parse.ReplaceSentenceHintParser
import dev.kensa.parse.java.Java20Lexer.*
import dev.kensa.parse.java.Java20Parser.TextBlock
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class JavaMethodBodyParser(
    private val stateMachine: ParserStateMachine,
    private val parseContext: ParseContext,
    private val hintParser: ReplaceSentenceHintParser
) : Java20ParserBaseListener() {

    private var replacedStatementDepth = 0

    //  For Debugging:
    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun enterMethodBody(ctx: Java20Parser.MethodBodyContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterMethod)
    }

    override fun exitMethodBody(ctx: Java20Parser.MethodBodyContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitMethod)
    }

    override fun enterBlock(ctx: Java20Parser.BlockContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterBlock)
    }

    override fun exitBlock(ctx: Java20Parser.BlockContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitBlock)
    }

    override fun enterStatement(ctx: Java20Parser.StatementContext) {
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

    override fun exitStatement(ctx: Java20Parser.StatementContext) {
        if (replacedStatementDepth > 0) {
            replacedStatementDepth--
            if (replacedStatementDepth == 0) stateMachine.apply(ExitStatement)
            return
        }
        stateMachine.apply(ExitStatement)
    }

    override fun enterMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ctx.asMethodInvocation())
    }

    override fun exitMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitMethodInvocation)
    }

    override fun enterMethodName(ctx: Java20Parser.MethodNameContext) {
        if (replacedStatementDepth > 0) return
        with(parseContext) {
            stateMachine.apply(ctx.asMethod() ?: ctx.asExpandableSentence()?.let { expandable ->
                if (ctx.hasArguments())
                    expandable.asExpandableSentenceWithArguments()
                else expandable
            } ?: ctx.asIdentifier())
        }
    }

    override fun exitArgumentList(ctx: Java20Parser.ArgumentListContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitValueArguments)
    }

    override fun enterIdentifier(ctx: Java20Parser.IdentifierContext) {
        if (replacedStatementDepth > 0) return
        with(parseContext) {
            stateMachine.apply(ctx.asField() ?: ctx.asParameter() ?: ctx.asIdentifier())
        }
    }

    override fun enterExpression(ctx: Java20Parser.ExpressionContext) {
        if (replacedStatementDepth > 0) return
        with(parseContext) {
            when {
                ctx.matchesFixturesExpression() -> ctx.asFixture()
                ctx.matchesOutputsExpression() -> ctx.asOutputs()
                ctx.matchesRenderedValueMethodExpression() -> ctx.asRenderedValueMethodExpression()
                ctx.matchesChainedCall() -> ctx.asChainedCall()
                else -> ctx.asEnterExpression()
            }?.also { stateMachine.apply(it) }
        }
    }

    override fun exitExpression(ctx: Java20Parser.ExpressionContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitExpression)
    }

    override fun enterTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(EnterTypeArguments)
    }

    override fun exitTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        if (replacedStatementDepth > 0) return
        stateMachine.apply(ExitTypeArguments)
    }

    override fun visitTerminal(node: TerminalNode) {
        if (replacedStatementDepth > 0) return
        with(parseContext) {
            with(node) {
                when (symbol.type) {
                    RPAREN, RBRACE -> node.asNote()?.also { stateMachine.apply(it) }
                    BooleanLiteral -> stateMachine.apply(asBooleanLiteral())
                    IntegerLiteral, FloatingPointLiteral -> stateMachine.apply(asNumberLiteral())
                    CharacterLiteral -> stateMachine.apply(asCharacterLiteral())
                    StringLiteral -> stateMachine.apply(asStringLiteral())
                    TextBlock -> stateMachine.apply(asMultilineString())
                    NullLiteral -> stateMachine.apply(asNullLiteral())
                }
            }
        }
    }

    // Looks for the appropriate sibling
    private fun ParserRuleContext.hasArguments(): Boolean = (parent as ParserRuleContext).hasChildOfType<Java20Parser.ArgumentListContext>()
    private inline fun <reified T : ParseTree> ParserRuleContext.hasChildOfType(): Boolean = children.any { it is T }

}