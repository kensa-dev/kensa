package dev.kensa.parse.java

import dev.kensa.parse.Event.*
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
import dev.kensa.parse.ParseContext.Companion.asStringLiteral
import dev.kensa.parse.ParserStateMachine
import dev.kensa.parse.java.Java20Lexer.*
import dev.kensa.parse.java.Java20Parser.TextBlock
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class JavaMethodBodyParser(
    private val stateMachine: ParserStateMachine,
    private val parseContext: ParseContext
) : Java20ParserBaseListener() {

    //  For Debugging:
    override fun enterEveryRule(ctx: ParserRuleContext) {
//        println("Entering: ${ctx::class} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
//        println("Exiting: ${ctx::class} :: ${ctx.text} :: ${stateMachine.stateMachine.state}")
    }

    override fun enterMethodBody(ctx: Java20Parser.MethodBodyContext) {
        stateMachine.apply(EnterMethod)
    }

    override fun exitMethodBody(ctx: Java20Parser.MethodBodyContext) {
        stateMachine.apply(ExitMethod)
    }

    override fun enterBlock(ctx: Java20Parser.BlockContext) {
        stateMachine.apply(EnterBlock)
    }

    override fun exitBlock(ctx: Java20Parser.BlockContext) {
        stateMachine.apply(ExitBlock)
    }

    override fun enterStatement(ctx: Java20Parser.StatementContext) {
        ctx.asNote()?.also {
            stateMachine.apply(it)
        }

        stateMachine.apply(ctx.asEnterStatement())
    }

    override fun exitStatement(ctx: Java20Parser.StatementContext) {
        stateMachine.apply(ExitStatement)
    }

    override fun enterMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
        stateMachine.apply(ctx.asMethodInvocation())
    }

    override fun exitMethodInvocation(ctx: Java20Parser.MethodInvocationContext) {
        stateMachine.apply(ExitMethodInvocation)
    }

    override fun enterMethodName(ctx: Java20Parser.MethodNameContext) {
        with(parseContext) {
            stateMachine.apply(ctx.asMethod() ?: ctx.asNested()?.let { nested ->
                if (ctx.hasArguments())
                    nested.asNestedWithArguments()
                else nested
            } ?: ctx.asIdentifier())
        }
    }

    override fun exitArgumentList(ctx: Java20Parser.ArgumentListContext) {
        stateMachine.apply(ExitValueArguments)
    }

    override fun enterIdentifier(ctx: Java20Parser.IdentifierContext) {
        with(parseContext) {
            stateMachine.apply(ctx.asField() ?: ctx.asParameter() ?: ctx.asIdentifier())
        }
    }

    override fun enterExpression(ctx: Java20Parser.ExpressionContext) {
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
        stateMachine.apply(ExitExpression)
    }

    override fun enterTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        stateMachine.apply(EnterTypeArguments)
    }

    override fun exitTypeArguments(ctx: Java20Parser.TypeArgumentsContext) {
        stateMachine.apply(ExitTypeArguments)
    }

    override fun visitTerminal(node: TerminalNode) {
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

                    else -> stateMachine.apply(Terminal)
                }
            }
        }
    }

    // Looks for the appropriate sibling
    private fun ParserRuleContext.hasArguments(): Boolean = (parent as ParserRuleContext).hasChildOfType<Java20Parser.ArgumentListContext>()
    private inline fun <reified T : ParseTree> ParserRuleContext.hasChildOfType(): Boolean = children.any { it is T }

}