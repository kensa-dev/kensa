package dev.kensa.output.json

import dev.kensa.sentence.RenderedToken
import dev.kensa.state.TestMethodContainer

internal object IndexMetrics {
    private const val KEYWORD_CSS = "tk-kw"

    fun interactionCount(container: TestMethodContainer): Int =
        container.invocations.sumOf { invocation -> invocation.interactions.count { it.isRenderedInteraction() } }

    fun participantCounts(container: TestMethodContainer): Map<String, Int> {
        val counts = LinkedHashMap<String, Int>()
        container.invocations.forEach { invocation ->
            invocation.interactions.forEach { entry ->
                interactionKeyPattern.matchEntire(entry.key)?.let { match ->
                    counts.merge(match.groupValues[2].trim(), 1, Int::plus)
                    counts.merge(match.groupValues[3].trim(), 1, Int::plus)
                }
            }
        }
        return counts
    }

    fun assertionCount(container: TestMethodContainer): Int =
        container.invocations.sumOf { invocation ->
            invocation.sentences.count { sentence ->
                val first = sentence.tokens.firstOrNull()
                first != null && KEYWORD_CSS in first.cssClasses && first.value.startsWith("then", ignoreCase = true)
            }
        }

    fun expandableCount(container: TestMethodContainer): Int =
        container.invocations.sumOf { invocation ->
            invocation.sentences.count { sentence ->
                sentence.tokens.any { it is RenderedToken.RenderedExpandableToken || it is RenderedToken.RenderedExpandableTabularToken }
            }
        }
}
