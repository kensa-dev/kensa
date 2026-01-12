package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.Literal.*
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression.Type.*
import dev.kensa.parse.LocatedEvent.PathExpression.FixturesExpression
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class ParseContext(
    private val properties: Map<String, ElementDescriptor>,
    private val methods: Map<String, ElementDescriptor.MethodElementDescriptor>,
    private val parameters: Map<String, ElementDescriptor> = emptyMap(),
    private val nestedMethods: Map<String, ParsedNestedMethod> = emptyMap(),
    private val emphasisedMethods: Map<String, EmphasisDescriptor> = emptyMap()
) {

    private val renderedValueMethodNames = methods.filterValues { it.isRenderedValue && it.hasParameters }.keys
    private val methodNames = methods.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val fieldNames = properties.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val parameterNames = parameters.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val nestedMethodNames = nestedMethods.keys

    private val singleCallWithArgumentsPattern = """^(?:(?<receiver>\w+)\.)?(?<function>\w+)\(.*\)$""".toRegex()
    private val fixturesPattern = """^fixtures[\[(](?:(\w+)\.)?(\w+)[])](\.(.+))?$""".toRegex()
    private val outputsByNamePattern = """^outputs[\[(](?:(\w+)\.)?(\w+)[])](\.(.+))?$""".toRegex()
    private val outputsByKeyPattern = """^outputs\("([a-zA-Z0-9_]+)"\)(\.(.+))?$""".toRegex()
    private val chainedCallPattern = """^(\w+)(\(\))?(\.(.+))?$""".toRegex()

    private fun emphasis(name: String) = emphasisedMethods[name] ?: EmphasisDescriptor.Default
    private fun nestedSentences(name: String) = nestedMethods[name]?.sentences ?: error("No nested method found with name [$name]")

    internal fun ParseTree.asIdentifier() = Identifier(location, text, emphasis(text))
    internal fun ParseTree.asNested() = takeIf { nestedMethodNames.contains(text) }?.let { Nested(location, text, nestedSentences(text)) }
    internal fun ParseTree.asParameter() = takeIf { parameterNames.contains(text) }?.let { Parameter(location, text) }
    internal fun ParseTree.asField() = takeIf { fieldNames.contains(text) }?.let { Field(location, text) }
    internal fun ParseTree.asMethod() = takeIf { methodNames.contains(text) }?.let { Method(location, text) }
    internal fun ParseTree.asFixture() = fixturesPattern.matchEntire(text)?.let { FixturesExpression(location, it.groupValues[2], it.groupValues[4]) }
    internal fun ParseTree.asOutputs() = asOutputsByName() ?: asOutputsByKey()
    private fun ParseTree.asOutputsByName() = outputsByNamePattern.matchEntire(text)?.let { PathExpression.OutputsByNameExpression(location, it.groupValues[2], it.groupValues[4]) }
    private fun ParseTree.asOutputsByKey() = outputsByKeyPattern.matchEntire(text)?.let { PathExpression.OutputsByKeyExpression(location, it.groupValues[1], it.groupValues[2]) }

    fun copy(parameters: Map<String, ElementDescriptor>) = ParseContext(properties, methods, parameters, nestedMethods, emphasisedMethods)

    internal fun Nested.asNestedWithArguments() = NestedWithArguments(location, name, sentences)

    internal fun ParseTree.asChainedCall(): ChainedCallExpression? =
        chainedCallPattern.matchEntire(text)?.let { matchResult ->
            callTypeFor(matchResult.groupValues[1])?.let { type ->
                ChainedCallExpression(location, type, matchResult.groupValues[1], matchResult.groupValues[4])
            }
        }

    internal fun ParseTree.asRenderedValueMethodExpression(): RenderedValue? =
        singleCallWithArgumentsPattern.matchEntire(text)?.let { matchResult ->
            RenderedValue(location, matchResult.groups["function"]!!.value)
        }

    private fun ParseContext.callTypeFor(key: String): ChainedCallExpression.Type? =
        when {
            methods[key]?.isRenderedValue == true -> Method
            parameters[key]?.isRenderedValue == true -> Parameter
            properties[key]?.isRenderedValue == true -> Field
            else -> null
        }

    internal fun ParseTree?.matchesRenderedValueMethodExpression() = this?.text?.let { singleCallWithArgumentsPattern.matchEntire(it)?.let { result -> result.groups["function"]?.value in renderedValueMethodNames } } ?: false
    internal fun ParseTree?.matchesFixturesExpression() = this?.text?.matches(fixturesPattern) ?: false
    internal fun ParseTree?.matchesOutputsExpression() = this?.matchesOutputsByNameExpression() ?: false || this?.matchesOutputsByKeyExpression() ?: false
    private fun ParseTree.matchesOutputsByNameExpression() = text?.matches(outputsByNamePattern) ?: false
    private fun ParseTree.matchesOutputsByKeyExpression() = text?.matches(outputsByKeyPattern) ?: false
    internal fun ParseTree?.matchesChainedCall(): Boolean {
        if (this == null) return false
        val text = this.text ?: return false

        val matchResult = chainedCallPattern.matchEntire(text) ?: return false
        val firstGroup = matchResult.groupValues[1]

        return callTypeFor(firstGroup) != null
    }

    companion object {

        internal fun ParserRuleContext.asNote() = (start as? KensaToken)?.asNote()
        internal fun TerminalNode.asNote() = (this.symbol as? KensaToken)?.asNote()
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

        private fun KensaToken.asNote() = Note(Location(line, charPositionInLine), note)
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