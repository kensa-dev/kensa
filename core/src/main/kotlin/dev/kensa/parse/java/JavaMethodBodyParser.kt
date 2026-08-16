package dev.kensa.parse.java

import dev.kensa.parse.Event
import dev.kensa.parse.Event.*
import dev.kensa.parse.Location
import dev.kensa.parse.LocatedEvent.IgnoreLines
import dev.kensa.parse.LocatedEvent.PathExpression.ContainerChainExpression
import dev.kensa.parse.ParseContext
import dev.kensa.parse.ParseContext.Companion.asBooleanLiteral
import dev.kensa.parse.ParseContext.Companion.asCharacterLiteral
import dev.kensa.parse.ParseContext.Companion.asEnterExpression
import dev.kensa.parse.ParseContext.Companion.asEnterStatement
import dev.kensa.parse.ParseContext.Companion.asIgnoreHint
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

internal class JavaMethodBodyParser(
    private val stateMachine: ParserStateMachine,
    private val parseContext: ParseContext,
    private val hintParser: ReplaceSentenceHintParser
) : Java20ParserBaseListener() {

    private var replacedStatementDepth = 0
    private var suppressedConstantMember: Java20Parser.IdentifierContext? = null
    private val suppressedContainerMembers = HashSet<Java20Parser.IdentifierContext>()

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
        emitIgnoreHint(ctx)
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
        emitIgnoreHint(ctx)
        if (ctx === suppressedConstantMember) {
            suppressedConstantMember = null
            return
        }
        if (suppressedContainerMembers.remove(ctx)) return
        with(parseContext) {
            stateMachine.apply(ctx.asContainerChainOrNull() ?: ctx.asField() ?: ctx.asParameter() ?: ctx.asConstantReferenceOrNull() ?: ctx.asIdentifier())
        }
    }

    private fun Java20Parser.IdentifierContext.asContainerChainOrNull(): Event? {
        val packageName = parent as? Java20Parser.PackageNameContext ?: return null
        if (packageName.parent !is Java20Parser.TypeNameContext) return null

        val members = mutableListOf<Java20Parser.IdentifierContext>()
        var current = packageName.packageName()
        while (current != null) {
            members.add(current.identifier())
            current = current.packageName()
        }
        if (members.isEmpty()) return null

        val type = with(parseContext) { containerChainTypeFor(text, members.first().text) } ?: return null
        suppressedContainerMembers.addAll(members)
        return ContainerChainExpression(Location(start.line, start.charPositionInLine), type, text, members.joinToString(".") { it.text })
    }

    private fun Java20Parser.IdentifierContext.asConstantReferenceOrNull(): Event? {
        val ambiguousName = parent as? Java20Parser.AmbiguousNameContext ?: return null
        if (ambiguousName.ambiguousName() != null) return null
        val expressionName = ambiguousName.parent as? Java20Parser.ExpressionNameContext ?: return null
        val memberCtx = expressionName.identifier() ?: return null
        return with(parseContext) { asConstantReference(memberCtx.text) }?.also { suppressedConstantMember = memberCtx }
    }

    private fun emitIgnoreHint(ctx: ParserRuleContext) {
        ctx.asIgnoreHint()?.also { lineCount ->
            stateMachine.apply(IgnoreLines(Location(ctx.start.line, ctx.start.charPositionInLine), lineCount))
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