package dev.kensa.sentence

import dev.kensa.RenderedValueStyle
import dev.kensa.RenderedValueStyle.Tabular
import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.parse.Event.MultilineString
import dev.kensa.parse.LocatedEvent
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.Literal.*
import dev.kensa.parse.LocatedEvent.PathExpression.*
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression.Type.*
import dev.kensa.parse.Location
import dev.kensa.sentence.TemplateToken.*
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.scanner.Index
import dev.kensa.sentence.scanner.TokenScanner
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class SentenceBuilder(val isNoteBlock: Boolean, private val startingLocation: Location, previousLocation: Location, private val dictionary: Dictionary, private val tabSize: Int) {
    var lastLocation = startingLocation

    private val tokens: MutableList<TemplateToken> = ArrayList()
    private val scanner: TokenScanner = TokenScanner(dictionary)
    private var currentExpandableTemplateToken: TemplateToken? = null
    private var currentExpandableLocation: Location? = null

    init {
        if (startingLocation.lineNumber - previousLocation.lineNumber > 1) {
            tokens.append("", BlankLine)
        }
    }

    fun beginExpandableSentence(location: Location, placeholder: String, sentences: List<TemplateSentence>) {
        currentExpandableLocation = location
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)

        val templateToken = ExpandableTemplateToken(
            scannedPlaceholder(placeholder),
            EmphasisDescriptor.Default,
            setOf(Expandable),
            name = placeholder,
            expandableTokens = sentences.map { it.tokens }
        )

        currentExpandableTemplateToken = templateToken

        tokens.add(templateToken)
    }

    fun beginExpandableValue(location: Location, name: String, style: RenderedValueStyle, headers: List<String>) {
        val token = if (style == Tabular) {
            TabularTemplateToken(
                template = name,
                types = setOf(Expandable, Table),
                name = name,
                rows = emptyList(),
                headers = headers
            )
        } else {
            ExpandableTemplateToken(
                template = name,
                types = setOf(Expandable, MethodValue),
                name = name,
                expandableTokens = emptyList()
            )
        }
        pushExpandable(location, token)
    }

    private fun scannedPlaceholder(placeholder: String): String {
        val (scanned, indices) = scanner.scan(placeholder, false)
        return indices.joinToString(separator = " ") { index -> scanned.substring(index.start, index.end) }
    }

    private fun pushExpandable(location: Location, token: TemplateToken) {
        currentExpandableLocation = location
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)
        currentExpandableTemplateToken = token
        tokens.add(token)
    }

    fun finishExpandable(parameterEvents: List<LocatedEvent> = emptyList()) {
        var lastLocation = requireForExpandable(currentExpandableLocation)
        val currentTemplate = requireForExpandable(currentExpandableTemplateToken)

        val parameterTokens = mutableListOf<TemplateToken>()

        parameterEvents.mapNotNullTo(parameterTokens) { event ->
            lastLocation = parameterTokens.checkLineAndIndent(event.location, lastLocation)
            when (event) {
                is ChainedCallExpression -> when (event.type) {
                    Method -> event.asSentenceToken(MethodValue)
                    Field -> event.asSentenceToken(FieldValue)
                    Parameter -> event.asSentenceToken(ParameterValue)
                }

                is FixturesExpression -> event.asSentenceToken(FixturesValue)
                is OutputsByNameExpression -> event.asSentenceToken(OutputsValueByName)
                is StringLiteral -> {
                    if (dictionary.isAcronym(event.value)) {
                        SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(StringLiteral, Type.Acronym))
                    } else {
                        SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(StringLiteral))
                    }
                }

                is Identifier -> SimpleTemplateToken(event.name, event.emphasis, setOf(Identifier))
                is Operator -> SimpleTemplateToken(event.text, EmphasisDescriptor.Default, setOf(Operator))
                is NullLiteral -> SimpleTemplateToken("null", EmphasisDescriptor.Default, setOf(NullLiteral))
                is NumberLiteral -> SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(NumberLiteral))
                is CharacterLiteral -> SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(CharacterLiteral))
                is BooleanLiteral -> SimpleTemplateToken(event.value, EmphasisDescriptor.Default, setOf(BooleanLiteral))
                is Note -> SimpleTemplateToken(event.text, EmphasisDescriptor.Default, setOf(Note))

                else -> null
            }
        }

        when (currentTemplate) {
            is ExpandableTemplateToken -> currentTemplate.parameterTokens = parameterTokens
            is TabularTemplateToken -> currentTemplate.parameterTokens = parameterTokens
            else -> {}
        }
        currentExpandableTemplateToken = null
    }

    fun append(event: NumberLiteral) = appendLiteral(event, event.value, NumberLiteral)

    fun append(event: NullLiteral) = appendLiteral(event, "null", NullLiteral)

    fun append(event: CharacterLiteral) = appendLiteral(event, event.value.trim('\''), CharacterLiteral)

    fun append(event: BooleanLiteral) = appendLiteral(event, event.value, BooleanLiteral)

    fun append(event: StringLiteral) {
        lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)
        if (dictionary.isAcronym(event.value)) {
            tokens.append(event.value, StringLiteral, Type.Acronym)
        } else {
            tokens.append(event.value, StringLiteral)
        }
    }

    fun append(event: MultilineString) {
        tokens.append(event.value, TextBlock)
    }

    fun append(event: Note) {
        if (isNoteBlock) {
            val lastIndex = tokens.indexOfLast { it.types.contains(Note) }
            if (lastIndex < 0) {
                tokens.append(event.text, Note)
            } else {
                val last = tokens[lastIndex]
                val merged = SimpleTemplateToken(
                    template = last.template + "\n" + event.text,
                    emphasis = last.emphasis,
                    types = last.types
                )
                tokens[lastIndex] = merged
            }
        } else {
            lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)
            tokens.append(event.text, Note)
        }
    }

    fun append(event: Operator) = appendLiteral(event, event.text, Operator)

    fun appendFixturesValue(location: Location, name: String, path: String) {
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)
        tokens.add(SimpleTemplateToken("$name:$path", EmphasisDescriptor.Default, types = setOf(FixturesValue)))
    }

    fun appendOutputsByNameValue(location: Location, name: String, path: String) {
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)
        tokens.add(SimpleTemplateToken("$name:$path", EmphasisDescriptor.Default, types = setOf(OutputsValueByName)))
    }

    fun appendOutputsByKeyValue(location: Location, name: String, path: String) {
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)
        tokens.add(SimpleTemplateToken("$name:$path", EmphasisDescriptor.Default, types = setOf(OutputsValueByKey)))
    }

    fun append(location: Location, event: RenderedValue) {
        lastLocation = tokens.checkLineAndIndent(location, lastLocation)
        tokens.add(RenderedValueToken(event.name, EmphasisDescriptor.Default))
    }

    fun append(event: ChainedCallExpression) {
        when (event.type) {
            Method -> appendPathExpression(event, MethodValue)
            Field -> appendPathExpression(event, FieldValue)
            Parameter -> appendPathExpression(event, ParameterValue)
        }
    }

    fun append(event: Parameter) = appendNamedValue(event, event.name, ParameterValue)

    fun append(event: Field) = appendNamedValue(event, event.name, FieldValue)

    fun append(event: Method) = appendNamedValue(event, event.name, MethodValue)

    fun append(event: Identifier) {
        lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)

        val (scanned, indices) = scanner.scan(event.name, isFirstInSentence())
        indices.forEach { index: Index ->
            tokens.append(valueFor(index, scanned.substring(index.start, index.end)), index.type, emphasis = index.emphasis ?: event.emphasis)
        }
    }

    fun build(): TemplateSentence = TemplateSentence(tokens)

    @OptIn(ExperimentalContracts::class)
    private fun <T : Any> requireForExpandable(value: T?): T {
        contract {
            returns() implies (value != null)
        }
        return requireNotNull(value) {
            "Attempted to finish an expandable but no location was available"
        }
    }

    private fun isFirstInSentence() =
        tokens.find { token -> token.types.any { !it.isWhitespace } } == null

    private fun MutableList<TemplateToken>.checkLineAndIndent(thisLocation: Location, lastLocation: Location): Location =
        thisLocation.apply {
            if (lineNumber > lastLocation.lineNumber) {
                append("", NewLine)

                repeat(linePosition / tabSize) {
                    append("", Indent)
                }
            }
        }

    private fun valueFor(index: Index, rawValue: String): String =
        when (index.type) {
            Type.Acronym -> rawValue.uppercase(Locale.getDefault())
            Keyword -> rawValue.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            Word -> if (rawValue.length > 1 && rawValue.matches(ALPHANUMERIC_UNDERSCORE)) rawValue else rawValue.replaceFirstChar { it.lowercase(Locale.getDefault()) }

            else -> rawValue
        }

    private fun appendLiteral(event: LocatedEvent, value: String, type: Type) {
        lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)
        tokens.append(value, type)
    }

    private fun appendPathExpression(event: ChainedCallExpression, tokenType: Type) {
        lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)
        tokens.add(event.asSentenceToken(tokenType))
    }

    private fun appendNamedValue(event: LocatedEvent, name: String, tokenType: Type) {
        lastLocation = tokens.checkLineAndIndent(event.location, lastLocation)
        tokens.add(SimpleTemplateToken("$name:", EmphasisDescriptor.Default, types = setOf(tokenType)))
    }

    private fun PathExpression.asSentenceToken(tokenType: Type) = SimpleTemplateToken("${name}:${path}", EmphasisDescriptor.Default, types = setOf(tokenType))

    private fun MutableList<TemplateToken>.append(value: String, vararg tokenTypes: Type, emphasis: EmphasisDescriptor = EmphasisDescriptor.Default) {
        add(SimpleTemplateToken(value, emphasis = emphasis, types = setOf(*tokenTypes)))
    }

    companion object {
        private val ALPHANUMERIC_UNDERSCORE = "^[A-Z0-9_]+$".toRegex()
    }
}