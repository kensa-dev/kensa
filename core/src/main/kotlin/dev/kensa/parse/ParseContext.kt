package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.Literal.*
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression.Type.*
import dev.kensa.parse.LocatedEvent.PathExpression.FixturesExpression
import dev.kensa.parse.RegexPatterns.chainedCallPattern
import dev.kensa.parse.RegexPatterns.fixturesPattern
import dev.kensa.parse.RegexPatterns.outputsByKeyPattern
import dev.kensa.parse.RegexPatterns.outputsByNamePattern
import dev.kensa.parse.RegexPatterns.singleCallWithArgumentsPattern
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class ParseContext(
    private val properties: Map<String, ElementDescriptor>,
    private val methods: Map<String, ElementDescriptor.MethodElementDescriptor>,
    private val parameters: Map<String, ElementDescriptor> = emptyMap(),
    private val expandableMethods: Map<String, ParsedExpandableMethod> = emptyMap()
) {

    private val expandableRenderedValueMethodNames = methods.filterValues { it.isExpandableRenderedValue }.keys
    private val renderedValueMethodNames = methods.filterValues { it.isRenderedValue }.keys
    private val methodNames = methods.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val fieldNames = properties.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val parameterNames = parameters.filterValues { it.isRenderedValue || it.isHighlight }.keys
    private val expandableMethodNames = expandableMethods.keys

    private fun expandableSentencesFor(name: String) = expandableMethods[name]?.sentences ?: error("No expandable method found with name [$name]")

    internal fun ParseTree.asIdentifier() = Identifier(location, text)
    internal fun ParseTree.asExpandableSentence() = takeIf { expandableMethodNames.contains(text) }?.let { ExpandableSentence(location, text, expandableSentencesFor(text)) }
    internal fun ParseTree.asExpandableValue() = takeIf { expandableRenderedValueMethodNames.contains(text) }?.let {
        val md = methods[text]!!
        ExpandableValue(location, text, md.renderedValueStyle, md.renderedValueHeaders)
    }
    internal fun ParseTree.asParameter() = takeIf { parameterNames.contains(text) }?.let { Parameter(location, text) }
    internal fun ParseTree.asField() = takeIf { fieldNames.contains(text) }?.let { Field(location, text) }
    internal fun ParseTree.asMethod() = takeIf { methodNames.contains(text) }?.let { Method(location, text) }
    internal fun ParseTree.asFixture() = fixturesPattern.matchEntire(text)?.let { FixturesExpression(location, it.groupValues[1], it.groupValues[2]) }
    internal fun ParseTree.asOutputs() = asOutputsByName() ?: asOutputsByKey()
    private fun ParseTree.asOutputsByName() = outputsByNamePattern.matchEntire(text)?.let { PathExpression.OutputsByNameExpression(location, it.groupValues[1], it.groupValues[2]) }
    private fun ParseTree.asOutputsByKey() = outputsByKeyPattern.matchEntire(text)?.let { PathExpression.OutputsByKeyExpression(location, it.groupValues[1], it.groupValues[2]) }

    fun copy(parameters: Map<String, ElementDescriptor>) = ParseContext(properties, methods, parameters, expandableMethods)

    internal fun ExpandableSentence.asExpandableSentenceWithArguments() = ExpandableSentenceWithArguments(location, name, sentences)
    internal fun ExpandableValue.asExpandableValueWithArguments() = ExpandableValueWithArguments(location, name, style, headers)

    internal fun ParseTree.asChainedCall(): ChainedCallExpression? =
        chainedCallPattern.matchEntire(text)?.let { matchResult ->
            callTypeFor(matchResult.groupValues[1])?.let { type ->
                ChainedCallExpression(location, type, matchResult.groupValues[1], matchResult.groupValues[3])
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

    internal fun asEventFromExpression(expr: String, location: Location): LocatedEvent {
        fixturesPattern.matchEntire(expr)?.let {
            return FixturesExpression(location, it.groupValues[1], it.groupValues[2])
        }
        outputsByNamePattern.matchEntire(expr)?.let {
            return PathExpression.OutputsByNameExpression(location, it.groupValues[1], it.groupValues[2])
        }
        outputsByKeyPattern.matchEntire(expr)?.let {
            return PathExpression.OutputsByKeyExpression(location, it.groupValues[1], it.groupValues[2])
        }
        singleCallWithArgumentsPattern.matchEntire(expr)?.let { matchResult ->
            val fn = matchResult.groups["function"]?.value
            if (fn in renderedValueMethodNames) return RenderedValue(location, fn!!)
        }
        chainedCallPattern.matchEntire(expr)?.let { matchResult ->
            val key = matchResult.groupValues[1]
            callTypeFor(key)?.let { type ->
                return ChainedCallExpression(location, type, key, matchResult.groupValues[3])
            }
        }
        return Identifier(location, expr)
    }

    companion object {

        internal fun ParserRuleContext.asNote() = (start as? KensaToken)?.asNote()
        internal fun TerminalNode.asNote() = (this.symbol as? KensaToken)?.asNote()
        internal fun ParserRuleContext.asReplaceSentenceHint(): String? =
            (start as? KensaToken)?.hint
                ?.takeIf { it.startsWith("ReplaceSentence:") }
                ?.removePrefix("ReplaceSentence:")
                ?.trim()
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

        private fun KensaToken.asNote() = note.takeIf { it.isNotBlank() }?.let { Note(Location(line, charPositionInLine), it) }
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