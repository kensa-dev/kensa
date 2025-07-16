package dev.kensa.parse

import dev.kensa.ElementDescriptor
import dev.kensa.ElementDescriptor.ResolveHolderElementDescriptor
import dev.kensa.KensaException
import dev.kensa.context.RealNestedInvocation
import dev.kensa.context.NestedInvocationContextHolder.nestedSentenceInvocationContext
import dev.kensa.render.Renderers
import dev.kensa.sentence.RenderedToken
import dev.kensa.sentence.RenderedToken.RenderedNestedToken
import dev.kensa.sentence.RenderedToken.RenderedValueToken
import dev.kensa.sentence.TemplateToken
import dev.kensa.sentence.TemplateToken.NestedTemplateToken
import dev.kensa.sentence.TemplateToken.Type
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.util.NamedValue
import java.util.ArrayList

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
                token.hasType(FieldValue) -> token.asFieldValue()
                token.hasType(MethodValue) -> token.asMethodValue()
                token.hasType(ParameterValue) -> token.asParameterValue()
                token.hasType(FixturesValue) -> token.asFixtureValue()
                token.hasType(OutputsValue) -> token.asOutputValue()
                token is NestedTemplateToken -> token.asNested()

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

    private fun TemplateToken.asRenderedValue() =
        RenderedValueToken(
            template,
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet()
        )

    private fun NestedTemplateToken.asNested() =
        RenderedNestedToken(
            template,
            (types.map { it.asCss() } + emphasis.asCss()).toSortedSet(),
            name = name,
            parameterTokens = render(parameterTokens),
            nestedTokens = when (val invocation = nestedSentenceInvocationContext().nextInvocationFor(name)) {
                is RealNestedInvocation -> nestedTokens.map { tokens -> invocation.rebuildRenderer().render(tokens) }
                else -> nestedTokens.map { tokens -> render(tokens) }
            }
        )

    private fun TemplateToken.asFixtureValue() =
        template.split(":").let { (name, path) ->
            RenderedValueToken(renderers.renderValue(fixtureAndOutputAccessor.fixtureValue(name, path)), (types.map { it.asCss() } + emphasis.asCss()).toSortedSet())
        }

    private fun TemplateToken.asOutputValue() =
        template.split(":").let { (name, path) ->
            RenderedValueToken(renderers.renderValue(fixtureAndOutputAccessor.outputValue(name, path)), (types.map { it.asCss() } + emphasis.asCss()).toSortedSet())
        }

    private fun TemplateToken.asFieldValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            properties[name]?.let { pd ->
                if (pd is ResolveHolderElementDescriptor) {
                    renderers.renderValue(pd.resolveValue(testInstance, "$name.$path"))
                } else {
                    renderers.renderValue(pd.resolveValue(testInstance, path))
                }.asToken(FieldValue, pd.isHighlight)
            }
        } ?: throw KensaException("Token [${template}] with type FieldValue did not refer to an actual field")

    private fun TemplateToken.asMethodValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            methods[name]?.let { md ->
                renderers.renderValue(md.resolveValue(testInstance, path))
                    .asToken(MethodValue, md.isHighlight)
            }
        } ?: throw KensaException("Token [${template}] with type MethodValue did not refer to an actual method")

    private fun TemplateToken.asParameterValue(): RenderedToken =
        template.split(":").let { (name, path) ->
            parameters[name]?.let { pd ->
                renderers.renderValue(pd.resolveValue(arguments, path))
                    .asToken(ParameterValue, pd.isHighlight)
            }
        } ?: template.asToken(ParameterValue, false) // Test suite has not been executed with the kensa-agent

    private fun String.asToken(type: Type, shouldHighlight: Boolean) =
        RenderedValueToken(
            this,
            buildSet {
                add(type.asCss())
                if (shouldHighlight || isHighlighted()) add(Highlighted.asCss())
            }
        )

    private fun RealNestedInvocation.rebuildRenderer() =
        TokenRenderer(testInstance, this.arguments, renderers, fixtureAndOutputAccessor, this.parameters, properties, methods, highlightedValues)

    private fun Type.asCss(): String = "tk-$code"

    private fun String.isHighlighted() = highlightedValues.any { it.value == this }
}