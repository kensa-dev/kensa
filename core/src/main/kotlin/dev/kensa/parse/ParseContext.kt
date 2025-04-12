package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent.BooleanLiteral
import dev.kensa.parse.LocatedEvent.CharacterLiteral
import dev.kensa.parse.LocatedEvent.EnterExpression
import dev.kensa.parse.LocatedEvent.EnterMethodInvocation
import dev.kensa.parse.LocatedEvent.EnterStatement
import dev.kensa.parse.LocatedEvent.Field
import dev.kensa.parse.LocatedEvent.Identifier
import dev.kensa.parse.LocatedEvent.Method
import dev.kensa.parse.LocatedEvent.MethodName
import dev.kensa.parse.LocatedEvent.Nested
import dev.kensa.parse.LocatedEvent.NullLiteral
import dev.kensa.parse.LocatedEvent.NumberLiteral
import dev.kensa.parse.LocatedEvent.Operator
import dev.kensa.parse.LocatedEvent.Parameter
import dev.kensa.parse.LocatedEvent.ScenarioExpression
import dev.kensa.parse.LocatedEvent.StringLiteral
import dev.kensa.sentence.Sentence
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class ParseContext(
    properties: Map<String, Accessor.ValueAccessor>,
    methods: Map<String, Accessor.ValueAccessor.MethodAccessor>,
    parameters: Map<String, Accessor.ValueAccessor.ParameterAccessor> = emptyMap(),
    private val nestedMethods: Map<String, List<Sentence>> = emptyMap(),
    private val emphasisedMethods: Map<String, EmphasisDescriptor> = emptyMap()
) {

    private val scenarioNames = properties.filterValues { it.isScenario }.keys
    private val methodNames = methods.filterValues { it.isSentenceValue || it.isHighlight }.keys
    private val fieldNames = properties.filterValues { it.isSentenceValue || it.isHighlight }.keys
    private val parameterNames = parameters.filterValues { it.isSentenceValue || it.isHighlight }.keys
    private val nestedMethodNames = nestedMethods.keys

    private val scenarioNamePattern = scenarioNames.joinToString("|") { Regex.escape(it) }
    private val scenarioPattern = """^($scenarioNamePattern)\.(\w+)(\(\))?$""".toRegex()

    private fun emphasis(name: String) = emphasisedMethods[name] ?: EmphasisDescriptor.Default
    private fun nestedSentences(name: String) = nestedMethods[name] ?: error("No nested method found with name [$name]")

    internal fun ParseTree.asMethodName() = MethodName(location, text, emphasis(text))
    internal fun ParseTree.asIdentifier() = Identifier(location, text, emphasis(text))
    internal fun ParseTree.asNested() = takeIf { nestedMethodNames.contains(text) }?.let { Nested(location, text, nestedSentences(text)) }
    internal fun ParseTree.asParameter() = takeIf { parameterNames.contains(text) }?.let { Parameter(location, text) }
    internal fun ParseTree.asField() = takeIf { fieldNames.contains(text) }?.let { Field(location, text) }
    internal fun ParseTree.asMethod() = takeIf { methodNames.contains(text) }?.let { Method(location, text) }
    internal fun ParseTree.asScenario() = scenarioPattern.matchEntire(text)?.let { ScenarioExpression(location, it.groupValues[1], it.groupValues[2]) }

    companion object {

        internal fun ParseTree.asOperator() = Operator(location, text)
        internal fun ParseTree.asBooleanLiteral() = BooleanLiteral(location, text)
        internal fun ParseTree.asCharacterLiteral() = CharacterLiteral(location, text)
        internal fun ParseTree.asStringLiteral() = StringLiteral(location,  text.removeQuotes())
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