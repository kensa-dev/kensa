package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.context.NestedInvocationContextHolder.expandableSentenceInvocationContext
import dev.kensa.context.RealNestedInvocation
import dev.kensa.context.RealRenderedValueInvocation
import dev.kensa.context.RenderedValueInvocationContextHolder.renderedValueInvocationContext
import dev.kensa.parse.ElementDescriptor.*
import dev.kensa.render.Renderers
import dev.kensa.sentence.RenderedToken
import dev.kensa.sentence.RenderedToken.RenderedExpandableToken
import dev.kensa.sentence.RenderedToken.RenderedValueToken
import dev.kensa.sentence.TemplateToken
import dev.kensa.sentence.TemplateToken.ExpandableTemplateToken
import dev.kensa.sentence.TemplateToken.TabularTemplateToken
import dev.kensa.sentence.TemplateToken.Type
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.util.NamedValue

class TokenRenderer(
    private val testInstance: Any,
    private val arguments: Array<Any?>,
    private val renderers: Renderers,
    private val fixtureAndOutputAccessor: FixtureAndOutputAccessor,
    private val parameters: Map<String, ElementDescriptor>,
    private val properties: Map<String, ElementDescriptor>,
    private val methods: Map<String, ElementDescriptor>,
    private val highlightedValues: Set<NamedValue>
) {

    fun render(tokens: List<TemplateToken>): List<RenderedToken> =
        tokens.squash().map { token ->
            when {
                token is TemplateToken.RenderedValueToken -> token.asRenderedValueToken()
                token.hasType(FieldValue) -> token.asFieldValue()
                token.hasType(MethodValue) -> token.asMethodValue()
                token.hasType(ParameterValue) -> token.asParameterValue()
                token.hasType(FixturesValue) -> token.asFixtureValue()
                token.hasType(OutputsValueByName) -> token.asOutputValueByName()
                token.hasType(OutputsValueByKey) -> token.asOutputValueByKey()
                token is ExpandableTemplateToken -> token.asExpandable()
                token is TabularTemplateToken -> token.asTabular()

                else -> token.asRenderedValue()
            }
        }

    private fun List<TemplateToken>.squash() = ArrayList<TemplateToken>().apply {
        var currentTokenTypes: Set<Type> = emptySet()
        var currentValue = ""
        var currentEmphasis = EmphasisDescriptor.Default

        fun finishCurrentWord() {
            if (currentTokenTypes.contains(Word)) {
                add(TemplateToken.SimpleTemplateToken(currentValue, emphasis = currentEmphasis, currentTokenTypes))
                currentValue = ""
                currentEmphasis = EmphasisDescriptor.Default
            }
        }

        this@squash.forEach { token ->
            if (token.hasType(Word)) {
                currentValue = if (currentTokenTypes.contains(Word)) {
                    if (currentEmphasis == token.emphasis) {
                        "$currentValue ${token.template}"
                    } else {
                        finishCurrentWord()
                        token.template
                    }
                } else {
                    token.template
                }
            } else {
                finishCurrentWord()
                add(token)
            }
            currentTokenTypes = token.types
            currentEmphasis = token.emphasis
        }
        finishCurrentWord()
    }

    private fun TemplateToken.asRenderedValueToken(): RenderedValueToken {
        val returnValue = when (val invocation = renderedValueInvocationContext().nextInvocationFor(template)) {
            is RealRenderedValueInvocation -> invocation.returnValue
            else -> template
        }
        return RenderedValueToken(
            renderers.renderValue(returnValue),
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet()
        )
    }

    private fun TemplateToken.asRenderedValue() =
        RenderedValueToken(
            template,
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet()
        )

    private fun ExpandableTemplateToken.asExpandable() =
        RenderedExpandableToken(
            template,
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet(),
            name = name,
            parameterTokens = render(parameterTokens),
            expandableTokens = when (val invocation = expandableSentenceInvocationContext().nextInvocationFor(name)) {
                is RealNestedInvocation -> expandableTokens.map { tokens -> invocation.rebuildRenderer().render(tokens) }
                else -> expandableTokens.map { tokens -> render(tokens) }
            }
        )

    private fun TabularTemplateToken.asTabular(): RenderedToken {
        val returnValue = when (val invocation = renderedValueInvocationContext().nextInvocationFor(name)) {
            is RealRenderedValueInvocation -> invocation.returnValue
            else -> null
        }

        val renderedRows = if (returnValue != null) {
            renderers.renderTable(returnValue).map { row ->
                row.map { cellValue ->
                    RenderedValueToken(
                        renderers.renderValue(cellValue),
                        setOf(Word.asCss())
                    )
                }
            }
        } else {
            rows.map { render(it) }
        }

        return RenderedToken.RenderedExpandableTabularToken(
            template,
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet(),
            name = name,
            parameterTokens = render(parameterTokens),
            rows = renderedRows,
            headers = headers
        )
    }

    private fun TemplateToken.asFixtureValue() =
        asRenderedValueToken { name, path -> fixtureAndOutputAccessor.fixtureValue(name, path) }

    private fun TemplateToken.asOutputValueByName() =
        asRenderedValueToken { name, path -> fixtureAndOutputAccessor.outputValueByName(name, path) }

    private fun TemplateToken.asOutputValueByKey() =
        asRenderedValueToken { name, path -> fixtureAndOutputAccessor.outputValueByKey(name, path) }

    private fun TemplateToken.asRenderedValueToken(getIt: (String, String) -> Any?) =
        template.split(":").let { (name, path) ->
            RenderedValueToken(renderers.renderValue(getIt(name, path)), (types.map { it.asCss() } + emphasis.asCss()).toSortedSet())
        }

    private fun TemplateToken.asFieldValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            properties[name]?.let { pd ->
                when (pd) {
                    is HintedPropertyElementDescriptor -> {
                        pd.resolveValue(testInstance, path)?.let { v ->
                            asToken(renderers.renderValue(v.value), Word, pd.isHighlight, v.hint)
                        }
                    }

                    is HintedEnumConstantElementDescriptor -> {
                        pd.resolveValue(testInstance, path).let { v ->
                            asToken(renderers.renderValue(v.value), Word, pd.isHighlight, v.hint)
                        }
                    }

                    else -> asToken(
                        if (pd is ResolveHolderElementDescriptor) {
                            renderers.renderValue(pd.resolveValue(testInstance, "$name.$path"))
                        } else {
                            renderers.renderValue(pd.resolveValue(testInstance, path))
                        }, FieldValue, pd.isHighlight
                    )
                }
            }
        } ?: throw KensaException("Token [${template}] with type FieldValue did not refer to an actual field")

    private fun TemplateToken.asMethodValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            methods[name]?.let { md ->
                asToken(renderers.renderValue(md.resolveValue(testInstance, path)), MethodValue, md.isHighlight)
            }
        } ?: throw KensaException("Token [${template}] with type MethodValue did not refer to an actual method")

    private fun TemplateToken.asParameterValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            parameters[name]?.let { pd ->
                asToken(renderers.renderValue(pd.resolveValue(arguments, path)), ParameterValue, pd.isHighlight)
            }
        } ?: asToken(template, ParameterValue, false) // Test suite has not been executed with the kensa-agent

    private fun asToken(value: String, type: Type, shouldHighlight: Boolean, hint: String? = null) =
        RenderedValueToken(
            value,
            buildSet {
                add(type.asCss())
                if (shouldHighlight || value.isHighlighted()) add(Highlighted.asCss())
                if (hint != null) add(Hinted.asCss())
            },
            hint
        )

    private fun RealNestedInvocation.rebuildRenderer() =
        TokenRenderer(testInstance, this.arguments, renderers, fixtureAndOutputAccessor, this.parameters, properties, methods, highlightedValues)

    private fun Type.asCss(): String = "tk-$code"

    private fun String.isHighlighted() = highlightedValues.any { it.value == this }
}