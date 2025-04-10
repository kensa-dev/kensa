package dev.kensa.parse

import dev.kensa.KensaException
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

sealed class Event<PT : ParseTree>(val parseTree: PT) {

    val location: Location
        get() = Location(lineNumber, linePosition)

    private val lineNumber: Int
        get() = when (parseTree) {
            is ParserRuleContext -> parseTree.start.line
            is TerminalNode -> parseTree.symbol.line

            else -> throw KensaException("Could not get line number from parse tree of type [${parseTree.javaClass}")
        }

    private val linePosition: Int
        get() = when (parseTree) {
            is ParserRuleContext -> parseTree.start.charPositionInLine
            is TerminalNode -> parseTree.symbol.charPositionInLine

            else -> throw KensaException("Could not get line position from parse tree of type [${parseTree.javaClass}")
        }

    override fun toString(): String = "${javaClass.simpleName}(${parseTree.text})"

    class EnterMethodEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitMethodEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class TerminalNodeEvent(parseTree: TerminalNode) : Event<TerminalNode>(parseTree)
    class EnterMethodInvocationEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitMethodInvocationEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class EnterTypeArgumentsEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitTypeArgumentsEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class IdentifierEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class EnterStatementEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitStatementEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class EnterExpressionEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)
    class ExitExpressionEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree)

    class OperatorEvent(parseTree: ParseTree) : Event<ParseTree>(parseTree) {
        val value: String = parseTree.text
    }

    sealed class LiteralEvent(parseTree: ParseTree, val value: String) : Event<ParseTree>(parseTree) {
        class BooleanLiteralEvent(parseTree: ParseTree) : LiteralEvent(parseTree, parseTree.text)
        class CharacterLiteralEvent(parseTree: ParseTree) : LiteralEvent(parseTree, parseTree.text)
        class StringLiteralEvent(parseTree: ParseTree, value: String) : LiteralEvent(parseTree, value)
        class MultiLineStringEvent(parseTree: ParseTree) : LiteralEvent(parseTree, parseTextBlock(parseTree.text))
        class NullLiteralEvent(parseTree: ParseTree) : LiteralEvent(parseTree, parseTree.text)
        class NumberLiteralEvent(parseTree: ParseTree) : LiteralEvent(parseTree, parseTree.text)
    }

    data class Location(val lineNumber: Int, val linePosition: Int)

    companion object {
        fun parseTextBlock(input: String): String {
            val strippedLines = input.lines().drop(1)
            val blockIndent = input.lines().last().indexOf("\"\"\"")

            return strippedLines.dropLast(1).joinToString("\n") { line ->
                var count = blockIndent
                line.dropWhile { c -> c.isWhitespace() && count-- > 0 }
            }
        }
    }
}