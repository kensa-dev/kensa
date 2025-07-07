package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent
import dev.kensa.parse.LocatedEvent.BooleanLiteral
import dev.kensa.parse.LocatedEvent.ChainedCallExpression
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Field
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Method
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.Parameter
import dev.kensa.parse.LocatedEvent.Field
import dev.kensa.parse.LocatedEvent.Identifier
import dev.kensa.parse.LocatedEvent.Method
import dev.kensa.parse.LocatedEvent.Operator
import dev.kensa.parse.LocatedEvent.Parameter
import dev.kensa.parse.LocatedEvent.StringLiteral
import dev.kensa.parse.Location
import dev.kensa.sentence.TemplateToken.NestedTemplateToken
import dev.kensa.sentence.TemplateToken.SimpleTemplateToken
import dev.kensa.sentence.TemplateToken.Type
import dev.kensa.sentence.TemplateToken.Type.NullLiteral
import dev.kensa.sentence.scanner.Index
import dev.kensa.sentence.scanner.TokenScanner
import java.util.*

class SentenceBuilder(var lastLocation: Location, private val dictionary: Dictionary, private val tabSize: Int) {
    private val tokens: MutableList<TemplateToken> = ArrayList()
    private val scanner: TokenScanner = TokenScanner(dictionary)

    fun appendNested(location: Location, placeholder: String, parameterEvents: List<LocatedEvent>, sentences: List<TemplateSentence>) {
        checkLineAndIndent(location)

        val (scanned, indices) = scanner.scan(placeholder, false)
        val scannedPlaceholder = indices.joinToString(separator = " ") { index -> scanned.substring(index.start, index.end) }

        val parameterTokens = parameterEvents.mapNotNull { event ->
            when (event) {
                is ChainedCallExpression -> when (event.type) {
                    Method -> event.asSentenceToken(Type.MethodValue)
                    Field -> event.asSentenceToken(Type.FieldValue)
                    Parameter -> event.asSentenceToken(Type.ParameterValue)
                }

                is StringLiteral -> SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(Type.StringLiteral))
                is Identifier -> SimpleTemplateToken(event.name, event.emphasis, setOf(Type.Identifier))
                is Operator -> SimpleTemplateToken(event.text, EmphasisDescriptor.Default, setOf(Type.Operator))
                else -> null
            }
        }
        tokens.add(NestedTemplateToken(
            scannedPlaceholder,
            EmphasisDescriptor.Default,
            setOf(Type.Expandable),
            name = placeholder,
            parameterTokens = parameterTokens,
            nestedTokens = sentences.map { it.tokens }
        ))
    }

    fun append(event: LocatedEvent.NumberLiteral) {
        checkLineAndIndent(event.location)
        append(event.value, Type.NumberLiteral)
    }

    fun append(event: LocatedEvent.NullLiteral) {
        checkLineAndIndent(event.location)
        append("null", NullLiteral)
    }

    fun append(event: LocatedEvent.CharacterLiteral) {
        checkLineAndIndent(event.location)
        append(event.value.trim('\''), Type.CharacterLiteral)
    }

    fun append(event: BooleanLiteral) {
        checkLineAndIndent(event.location)
        append(event.value, Type.BooleanLiteral)
    }

    fun append(event: StringLiteral) {
        checkLineAndIndent(event.location)
        if (dictionary.isAcronym(event.value)) {
            append(event.value, Type.StringLiteral, Type.Acronym)
        } else {
            append(event.value, Type.StringLiteral)
        }
    }

    fun append(event: MultilineString) {
        append(event.value, Type.TextBlock)
    }

    fun append(event: Operator) {
        checkLineAndIndent(event.location)
        append(event.text, Type.Operator)
    }

    fun appendFixturesValue(location: Location, name: String, path: String) {
        checkLineAndIndent(location)
        tokens.add(SimpleTemplateToken("$name:$path", EmphasisDescriptor.Default, types = setOf(Type.FixturesValue)))
    }

    fun append(event: ChainedCallExpression) {
        checkLineAndIndent(event.location)
        when (event.type) {
            Method -> tokens.add(event.asSentenceToken(Type.MethodValue))
            Field -> tokens.add(event.asSentenceToken(Type.FieldValue))
            Parameter -> tokens.add(event.asSentenceToken(Type.ParameterValue))
        }
    }

    fun append(event: Parameter) {
        checkLineAndIndent(event.location)
        tokens.add(SimpleTemplateToken("${event.name}:", EmphasisDescriptor.Default, types = setOf(Type.ParameterValue)))
    }

    fun append(event: Field) {
        checkLineAndIndent(event.location)
        tokens.add(SimpleTemplateToken("${event.name}:", EmphasisDescriptor.Default, types = setOf(Type.FieldValue)))
    }

    fun append(event: Method) {
        checkLineAndIndent(event.location)
        tokens.add(SimpleTemplateToken("${event.name}:", EmphasisDescriptor.Default, types = setOf(Type.MethodValue)))
    }

    fun append(event: Identifier) {
        checkLineAndIndent(event.location)

        val (scanned, indices) = scanner.scan(event.name, isFirstInSentence())
        indices.forEach { index: Index ->
            append(valueFor(index, scanned.substring(index.start, index.end)), index.type, emphasis = index.emphasis ?: event.emphasis)
        }
    }

    fun build(): TemplateSentence = TemplateSentence(tokens)

    private fun ChainedCallExpression.asSentenceToken(tokenType: Type) = SimpleTemplateToken("${name}:${path}", EmphasisDescriptor.Default, types = setOf(tokenType))

    private fun isFirstInSentence() =
        tokens.find { token -> token.types.any { !it.isWhitespace } } == null

    private fun checkLineAndIndent(location: Location) {
        if (location.lineNumber - lastLocation.lineNumber > 1) {
            append("", Type.BlankLine)
        }
        if (location.lineNumber > lastLocation.lineNumber && location.linePosition > lastLocation.linePosition) {
            append("", Type.NewLine)
            repeat(location.linePosition / tabSize) {
                append("", Type.Indent)
            }
        }
        lastLocation = Location(location.lineNumber, lastLocation.linePosition)
    }

    private fun append(value: String, vararg tokenTypes: Type, emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) {
        tokens.add(SimpleTemplateToken(value, emphasis = emphasis, types = setOf(*tokenTypes)))
    }

    private fun valueFor(index: Index, rawValue: String): String =
        when (index.type) {
            Type.Acronym -> rawValue.uppercase(Locale.getDefault())
            Type.Keyword -> rawValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            Type.Word -> if (rawValue.length > 1 && rawValue.matches(ALPHANUMERIC_UNDERSCORE)) rawValue else rawValue.replaceFirstChar { it.lowercase(Locale.getDefault()) }

            else -> rawValue
        }

    companion object {
        private val ALPHANUMERIC_UNDERSCORE = "^[A-Z0-9_]+$".toRegex()
    }
}