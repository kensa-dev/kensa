package dev.kensa.output.search

import com.eclipsesource.json.Json
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.render.Renderers
import dev.kensa.sentence.RenderedToken
import dev.kensa.state.CapturedInteractions.Companion.sdMarkerKey
import dev.kensa.state.TestInvocation
import dev.kensa.util.KensaMap
import java.nio.file.Path
import kotlin.io.path.writeText
import com.eclipsesource.json.Json.`object` as jsonObject

data class SearchLocation(val testId: String, val testMethod: String, val invocation: Int, val displayName: String, val count: Int)

data class SearchTerm(val value: String, val names: List<String>, val locations: List<SearchLocation>)

class SearchIndexBuilder(private val renderers: Renderers) {

    private val braceKeyPattern = "^\\{.+}.*$".toRegex()

    fun build(containers: List<TestContainer>): List<SearchTerm> {
        val namesByValue = mutableMapOf<String, MutableSet<String>>()

        data class InvocationText(val testId: String, val testMethod: String, val invocation: Int, val displayName: String, val text: String)

        val invocationTexts = mutableListOf<InvocationText>()

        containers.forEach { container ->
            val testId = container.testClass.name
            container.orderedMethodContainers.forEach { methodContainer ->
                val testMethod = methodContainer.method.name
                methodContainer.invocations.forEachIndexed { invocationIndex, invocation ->
                    (invocation.fixturesNamesAndValues + invocation.parameters).forEach { nv ->
                        val rendered = renderers.renderValue(nv.value)
                        if (!isNoise(rendered)) {
                            namesByValue.getOrPut(rendered) { sortedSetOf() }.add(nv.name)
                        }
                    }
                    val invocationDisplayName = invocation.parameterizedTestDescription ?: invocation.displayName
                    invocationTexts.add(InvocationText(testId, testMethod, invocationIndex, invocationDisplayName, textOf(invocation)))
                }
            }
        }

        val searchableValues = namesByValue.keys.toList()

        return searchableValues
            .map { value ->
                val names = namesByValue.getValue(value).sorted()
                val locations = invocationTexts
                    .mapNotNull { it ->
                        val count = countOccurrences(it.text, value)
                        if (count > 0) SearchLocation(it.testId, it.testMethod, it.invocation, it.displayName, count) else null
                    }
                    .sortedWith(compareBy({ it.testId }, { it.testMethod }, { it.invocation }))
                SearchTerm(value, names, locations)
            }
            .sortedBy { it.value }
    }

    private fun textOf(invocation: TestInvocation): String {
        val parts = mutableListOf<String>()

        invocation.sentences.forEach { sentence ->
            sentence.tokens.forEach { collectTokenText(it, parts) }
        }
        invocation.outputNamesAndValues.forEach { parts.add(renderers.renderValue(it.value)) }
        invocation.fixturesNamesAndValues.forEach { parts.add(renderers.renderValue(it.value)) }
        invocation.parameters.forEach { parts.add(renderers.renderValue(it.value)) }
        invocation.interactions
            .filter { it.key != sdMarkerKey && !it.key.matches(braceKeyPattern) }
            .forEach { entry -> collectInteractionText(entry, parts) }

        return parts.joinToString("\n")
    }

    private fun collectInteractionText(entry: KensaMap.Entry, parts: MutableList<String>) {
        entry.value?.let { value ->
            renderers.renderInteraction(value, entry.attributes).forEach { parts.add(it.value) }
        }
    }

    private fun collectTokenText(token: RenderedToken, parts: MutableList<String>) {
        parts.add(token.value)
        when (token) {
            is RenderedToken.RenderedExpandableTabularToken -> {
                token.parameterTokens.forEach { collectTokenText(it, parts) }
                token.headers.forEach { parts.add(it) }
                token.rows.forEach { row -> row.forEach { collectTokenText(it, parts) } }
            }

            is RenderedToken.RenderedExpandableToken -> {
                token.parameterTokens.forEach { collectTokenText(it, parts) }
                token.expandableTokens.forEach { group -> group.forEach { collectTokenText(it, parts) } }
            }

            else -> {}
        }
    }

    private fun countOccurrences(text: String, value: String): Int {
        var count = 0
        var from = 0
        while (true) {
            val i = text.indexOf(value, from)
            if (i < 0) break
            count++
            from = i + value.length
        }
        return count
    }

    private fun isNoise(value: String): Boolean {
        if (value.isBlank()) return true
        if (value == "null") return true
        if (value.length < 4) return true
        if (value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)) return true
        if (value.all { it.isDigit() } && value.length < 4) return true
        return false
    }
}

class SearchIndexWriter {

    fun write(outputDir: Path, terms: List<SearchTerm>) {
        val json = jsonObject()
            .add("schemaVersion", 1)
            .add("terms", Json.array().apply {
                terms.forEach { term ->
                    add(
                        jsonObject()
                            .add("value", term.value)
                            .add("names", Json.array().apply { term.names.forEach { add(it) } })
                            .add("locations", Json.array().apply {
                                term.locations.forEach { loc ->
                                    add(
                                        jsonObject()
                                            .add("testId", loc.testId)
                                            .add("testMethod", loc.testMethod)
                                            .add("invocation", loc.invocation)
                                            .add("displayName", loc.displayName)
                                            .add("count", loc.count)
                                    )
                                }
                            })
                    )
                }
            })

        outputDir.resolve("search-index.json").writeText(toJsonString()(json))
    }
}
