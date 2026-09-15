package dev.kensa.output.unfurl

import dev.kensa.Configuration
import dev.kensa.context.TestContainer
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

// One static page per class and per method under embed/<class>/, each
// carrying the card a link unfurler shows and a refresh to the live embed.
// Only a report with a published address can be unfurled, so nothing is
// written without a linkBaseUrl. The pages sit inside the bundle, so a site
// that copies bundles whole serves them unchanged.
internal class UnfurlWriter(private val configuration: Configuration) {

    fun write(container: TestContainer) {
        val base = configuration.linkBaseUrl ?: return
        val className = container.testClass.name
        val dir = configuration.outputDir.resolve("embed").resolve(className).createDirectories()
        val methods = container.orderedMethodContainers
        val icon = iconUrl(base)

        val count = if (methods.size == 1) "1 method" else "${methods.size} methods"
        dir.resolve("index.html").writeText(
            unfurlPage(
                title = container.displayName,
                siteName = configuration.titleText,
                description = unfurlDescription(container.state, count, emptyList()),
                url = embedUrl(base, configuration.sourceId, className, null),
                iconUrl = icon,
            )
        )

        methods.forEach { method ->
            val sentences = method.invocations.firstOrNull()?.sentences ?: emptyList()
            dir.resolve("${method.method.name}.html").writeText(
                unfurlPage(
                    title = method.displayName,
                    siteName = configuration.titleText,
                    description = unfurlDescription(method.state, container.displayName, sentences),
                    url = embedUrl(base, configuration.sourceId, className, method.method.name),
                    iconUrl = icon,
                )
            )
        }
    }
}
