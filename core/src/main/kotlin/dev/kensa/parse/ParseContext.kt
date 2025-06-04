package dev.kensa.parse

import dev.kensa.ElementDescriptor
import dev.kensa.KensaException
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Field
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Method
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Parameter
import dev.kensa.sentence.Sentence
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class ParseContext(
    private val properties: Map<String, ElementDescriptor>,
    private val methods: Map<String, ElementDescriptor>,
    private val parameters: Map<String, ElementDescriptor> = emptyMap(),
    private val nestedMethods: Map<String, List<Sentence>> = emptyMap(),
    private val emphasisedMethods: Map<String, EmphasisDescriptor> = emptyMap()
) {

    private val methodNames = methods.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val fieldNames = properties.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val parameterNames = parameters.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val nestedMethodNames = nestedMethods.keys

    private val fixturesPattern = """fixtures[\[(](\w+\.)?([^)\]]+)[)\]]""".toRegex()
    private val chainedCallPattern = """^([a-zA-Z_][a-zA-Z0-9_]*)\(?\)??(?:\.[a-zA-Z_][a-zA-Z0-9_]*(?:\(\))?)*$""".toRegex()

    private fun emphasis(name: String) = emphasisedMethods[name] ?: EmphasisDescriptor.Default
    private fun nestedSentences(name: String) = nestedMethods[name] ?: error("No nested method found with name [$name]")

    internal fun ParseTree.asMethodName() = MethodName(location, text, emphasis(text))
    internal fun ParseTree.asIdentifier() = Identifier(location, text, emphasis(text))
    internal fun ParseTree.asNested() = takeIf { nestedMethodNames.contains(text) }?.let { Nested(location, text, nestedSentences(text)) }
    internal fun ParseTree.asParameter() = takeIf { parameterNames.contains(text) }?.let { Parameter(location, text) }
    internal fun ParseTree.asField() = takeIf { fieldNames.contains(text) }?.let { Field(location, text) }
    internal fun ParseTree.asMethod() = takeIf { methodNames.contains(text) }?.let { Method(location, text) }
    internal fun ParseTree.asFixture() = fixturesPattern.matchEntire(text)?.let { FixturesExpression(location, it.groupValues[2]) }

    internal fun ParseTree.asChainedCall(): ChainedCallExpression? =
        chainedCallPattern.matchEntire(text)?.let { matchResult ->
            callTypeFor(matchResult.groupValues[1])?.let { type ->
                ChainedCallExpression(location, type, text)
            }
        }

    private fun ParseContext.callTypeFor(key: String): ChainedCallExpression.Type? =
        when {
            methods[key]?.isRenderedValue == true -> Method
            parameters[key]?.isRenderedValue == true -> Parameter
            properties[key]?.isRenderedValue == true -> Field
            else -> null
        }

    internal fun ParseTree?.matchesFixture() = this?.text?.matches(fixturesPattern) ?: false
    internal fun ParseTree?.matchesChainedCall(): Boolean {
        if (this == null) return false
        val text = this.text ?: return false

        val matchResult = chainedCallPattern.matchEntire(text) ?: return false
        val firstGroup = matchResult.groupValues[1]

        return callTypeFor(firstGroup) != null
    }

    companion object {

        internal fun ParseTree.asOperator() = Operator(location, text)
        internal fun ParseTree.asBooleanLiteral() = BooleanLiteral(location, text)
        internal fun ParseTree.asCharacterLiteral() = CharacterLiteral(location, text)
        internal fun ParseTree.asStringLiteral() = StringLiteral(location, text.removeQuotes())
        internal fun ParseTree.asNumberLiteral() = NumberLiteral(location, text)
        internal fun ParseTree.asNullLiteral() = NullLiteral(location)
        internal fun ParseTree.asMultilineString() = MultilineString(asTextBlock())

        internal fun ParseTree.asEnterStatement() = EnterStatement(location)
        internal fun ParseTree.asEnterExpression() = EnterExpression(location)
        internal fun ParseTree.asMethodInvocation() = EnterMethodInvocation(location)

        private fun String.removeQuotes(): String = removeSurrounding("\"")

        private fun ParseTree.asTextBlock(): String {
            val lines = text.lines()
            val strippedLines = lines.drop(1)
            val blockIndent = lines.last().indexOf("\"\"\"")

            return strippedLines.dropLast(1).joinToString("\n") { line ->
                var count = blockIndent
                line.dropWhile { c -> c.isWhitespace() && count-- > 0 }
            }
        }

        private val ParseTree.location: Location get() = Location(lineNumber, linePosition)

        private val ParseTree.lineNumber: Int
            get() = when (this) {
                is ParserRuleContext -> start.line
                is TerminalNode -> symbol.line

                else -> throw KensaException("Could not get line number from parse tree of type [${javaClass}")
            }

        private val ParseTree.linePosition: Int
            get() = when (this) {
                is ParserRuleContext -> start.charPositionInLine
                is TerminalNode -> symbol.charPositionInLine

                else -> throw KensaException("Could not get line position from parse tree of type [${javaClass}")
            }
    }
}