package dev.kensa.uitesting

import dev.kensa.tabs.KensaTabContext
import dev.kensa.tabs.KensaTabRenderer
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes

class ScreenshotsTabRenderer : KensaTabRenderer {

    override fun render(ctx: KensaTabContext): String? {
        val screenshots = ctx.attachments.getOrNull(SCREENSHOTS_KEY)
            ?.all()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        val safeClass = safeSegment(ctx.testClass)
        val safeMethod = safeSegment(ctx.testMethod)
        val tabIdSafe = safeSegment(ctx.tabId)

        val invocationDir = ctx.outputDir
            .resolve("tabs")
            .resolve(safeClass)
            .resolve(safeMethod)
            .resolve("invocation-${ctx.invocationIndex}")
            .resolve("$tabIdSafe-screenshots")
        invocationDir.createDirectories()

        return screenshots.mapIndexed { i, s ->
            val fileName = "$i-${safeSegment(s.label).ifEmpty { DEFAULT_SCREENSHOT_LABEL }}.${s.format}"
            invocationDir.resolve(fileName).writeBytes(s.bytes)
            val src = "tabs/$safeClass/$safeMethod/invocation-${ctx.invocationIndex}/$tabIdSafe-screenshots/$fileName"
            """{"label":${jsonString(s.label)},"src":${jsonString(src)}}"""
        }.joinToString(separator = ",", prefix = "[", postfix = "]")
    }

    override fun mediaType(): String = "application/vnd.kensa.screenshots+json"

    private fun safeSegment(input: String): String {
        val sanitised = input.replace(sanitisePattern, "_")
        return if (sanitised == "." || sanitised == "..") "_" else sanitised
    }

    private fun jsonString(s: String): String = buildString {
        append('"')
        for (c in s) when {
            c == '\\' || c == '"' -> append('\\').append(c)
            c == '\n' -> append("\\n")
            c == '\r' -> append("\\r")
            c == '\t' -> append("\\t")
            c < ' ' -> append("\\u%04x".format(c.code))
            else -> append(c)
        }
        append('"')
    }

    companion object {
        private const val DEFAULT_SCREENSHOT_LABEL = "screenshot"
        private val sanitisePattern = """[^A-Za-z0-9._-]""".toRegex()
    }
}
