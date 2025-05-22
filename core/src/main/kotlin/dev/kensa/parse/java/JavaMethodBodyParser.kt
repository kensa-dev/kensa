package dev.kensa.parse.java

import dev.kensa.parse.Event.*
import dev.kensa.parse.Java20Lexer.*
import dev.kensa.parse.Java20Parser
import dev.kensa.parse.Java20Parser.TextBlock
import dev.kensa.parse.Java20ParserBaseListener
import dev.kensa.parse.ParseContext
import dev.kensa.parse.ParseContext.Companion.asBooleanLiteral
import dev.kensa.parse.ParseContext.Companion.asCharacterLiteral
import dev.kensa.parse.ParseContext.Companion.asEnterExpression
import dev.kensa.parse.ParseContext.Companion.asEnterStatement
import dev.kensa.parse.ParseContext.Companion.asMethodInvocation
import dev.kensa.parse.ParseContext.Companion.asMultilineString
import dev.kensa.parse.ParseContext.Companion.asNullLiteral
import dev.kensa.parse.ParseContext.Companion.asNumberLiteral
import dev.kensa.parse.ParseContext.Companion.asStringLiteral
import dev.kensa.parse.ParserStateMachine
import org.antlr.v4.runtime.ParserRuleContext
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

    override fun enterMethodName(ctx: Java20Parser.MethodNameContext) =
        with(parseContext) {
            stateMachine.apply(ctx.asMethod() ?: ctx.asNested() ?: ctx.asMethodName())
        }

    override fun enterIdentifier(ctx: Java20Parser.IdentifierContext) =
        with(parseContext) {
            stateMachine.apply(ctx.asField() ?: ctx.asParameter() ?: ctx.asIdentifier())
        }

    override fun enterExpression(ctx: Java20Parser.ExpressionContext) =
        with(parseContext) {
            stateMachine.apply(ctx.asFixture() ?: ctx.asScenario() ?: ctx.asEnterExpression())
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
}