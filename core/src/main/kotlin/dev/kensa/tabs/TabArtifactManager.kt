package dev.kensa.tabs

import dev.kensa.Configuration
import dev.kensa.KensaTab
import dev.kensa.KensaTabVisibility.OnlyOnFailure
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState.Passed
import dev.kensa.util.findAllAnnotations
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class TabArtifactManager {

    data class InvocationKey(val testMethod: String, val invocationIndex: Int)

    fun generate(container: TestContainer, outputDir: Path, configuration: Configuration): Map<InvocationKey, List<JsonTransforms.CustomTabContent>> {
        val classTabs: List<KensaTab> = container.testClass.findAllAnnotations<KensaTab>().toList()

        if (classTabs.isEmpty() && container.orderedMethodContainers.none { it.method.findAllAnnotations<KensaTab>().isNotEmpty() }) {
            return emptyMap()
        }

        val services = DefaultKensaTabServices().apply {
            configuration.tabServiceFactories.forEach { (type, factory) ->
                @Suppress("UNCHECKED_CAST")
                register(type as KClass<Any>) { factory() }
            }
        }

        val result = mutableMapOf<InvocationKey, List<JsonTransforms.CustomTabContent>>()

        container.orderedMethodContainers.forEach { methodContainer ->
            val methodTabs: List<KensaTab> = methodContainer.method.findAllAnnotations<KensaTab>().toList()
            val tabsForMethod: List<KensaTab> = classTabs + methodTabs

            if (tabsForMethod.isEmpty()) return@forEach

            methodContainer.invocations.forEachIndexed { invocationIndex, invocation ->
                val key = InvocationKey(methodContainer.method.name, invocationIndex)

                val tabsForInvocation = generateForInvocation(
                    container = container,
                    methodContainer = methodContainer,
                    invocation = invocation,
                    invocationIndex = invocationIndex,
                    tabs = tabsForMethod,
                    outputDir = outputDir,
                    services = services
                )

                if (tabsForInvocation.isNotEmpty()) {
                    result[key] = tabsForInvocation
                }
            }
        }

        return result
    }

    private fun generateForInvocation(
        container: TestContainer,
        methodContainer: TestMethodContainer,
        invocation: TestInvocation,
        invocationIndex: Int,
        tabs: List<KensaTab>,
        outputDir: Path,
        services: KensaTabServices
    ): List<JsonTransforms.CustomTabContent> {
        val safeClass = safePathSegment(container.testClass.name)
        val safeMethod = safePathSegment(methodContainer.method.name)
        val usedIds = mutableMapOf<String, Int>()

        return tabs.mapNotNull { tab ->
            if (tab.visibility == OnlyOnFailure && invocation.state == Passed) {
                return@mapNotNull null
            }

            val baseId = baseTabId(tab)
            val stableTabId = disambiguate(baseId, usedIds)

            val tabIdForPath = safePathSegment(stableTabId)
            val relativeFile = "tabs/$safeClass/$safeMethod/invocation-$invocationIndex/$tabIdForPath.txt"

            val outputFile = outputDir.resolve(relativeFile)
            outputFile.parent.createDirectories()

            val baseCtx = KensaTabContext(
                tabId = stableTabId,
                tabName = tab.name,
                invocationIdentifier = null,
                testClass = container.testClass.name,
                testMethod = methodContainer.method.name,
                invocationIndex = invocationIndex,
                invocationDisplayName = invocation.parameterizedTestDescription ?: invocation.displayName,
                invocationState = invocation.state.description,
                fixtures = invocation.fixtures,
                capturedOutputs = invocation.outputs,
                services = services,
                sourceId = tab.sourceId
            )

            val identifierProvider: InvocationIdentifierProvider =
                tab.identifierProvider.objectInstance ?: tab.identifierProvider.createInstance()

            val invocationIdentifier = identifierProvider.identifier(baseCtx)

            val ctx = baseCtx.copy(invocationIdentifier = invocationIdentifier)

            val renderer: KensaTabRenderer =
                tab.renderer.objectInstance ?: tab.renderer.createInstance()

            val content = renderer.render(ctx)
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            outputFile.writeText(content)

            JsonTransforms.CustomTabContent(
                tabId = stableTabId,
                label = tab.name,
                file = relativeFile,
                mediaType = "text/plain"
            )
        }
    }

    private fun baseTabId(tab: KensaTab): String {
        tab.id.takeIf { it.isNotBlank() }?.let { return it }

        val rendererId = tab.renderer.qualifiedName ?: tab.renderer.simpleName ?: "Renderer"
        val providerId = tab.identifierProvider.qualifiedName ?: tab.identifierProvider.simpleName ?: "IdProvider"
        return "$rendererId:$providerId:${tab.name}"
    }

    private fun disambiguate(baseId: String, used: MutableMap<String, Int>): String {
        val count = (used[baseId] ?: 0) + 1
        used[baseId] = count
        return if (count == 1) baseId else "$baseId:$count"
    }

    private fun safePathSegment(input: String): String =
        input.replace(Regex("""[^A-Za-z0-9._-]"""), "_")
}