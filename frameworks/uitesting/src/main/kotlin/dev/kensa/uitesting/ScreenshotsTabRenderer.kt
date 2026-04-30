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

        val items = screenshots.mapIndexed { i, s ->
            val fileName = "$i-${safeSegment(s.label).ifEmpty { "screenshot" }}.${s.format}"
            invocationDir.resolve(fileName).writeBytes(s.bytes)
            val relativeSrc = "tabs/$safeClass/$safeMethod/invocation-${ctx.invocationIndex}/$tabIdSafe-screenshots/$fileName"
            s.label to relativeSrc
        }

        return buildStackedHtml(items)
    }

    override fun mediaType(): String = "text/html"

    private fun safeSegment(input: String): String {
        val sanitized = input.replace(Regex("""[^A-Za-z0-9._-]"""), "_")
        return if (sanitized == "." || sanitized == "..") "_" else sanitized
    }

    private fun buildStackedHtml(items: List<Pair<String, String>>): String {
        val sections = items.joinToString("\n") { (label, src) ->
            val caption = if (label.isNotBlank()) {
                """    <figcaption>${escapeHtml(label)}</figcaption>"""
            } else ""
            """  <figure class="shot">
$caption
    <img src="${escapeHtml(src)}" alt="${escapeHtml(label)}" onclick="expand(this)" />
  </figure>""".trimEnd()
        }
        return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"/>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: sans-serif; background: #f5f5f5; padding: 16px; }
  .shot { margin: 0 auto 24px; max-width: 720px; }
  .shot:last-child { margin-bottom: 0; }
  figcaption { font-size: 0.85em; color: #555; margin-bottom: 6px; }
  .shot img { display: block; max-width: 100%; max-height: 420px; height: auto; border: 1px solid #ddd; background: #fff; cursor: zoom-in; }
</style>
</head>
<body>
$sections
<script>
  function expand(img) {
    if (document.activeElement) document.activeElement.blur();
    window.parent.postMessage({type: 'kensa-expand-image', src: img.src, alt: img.alt}, '*');
    // Hand window focus back to the parent so its dialog handles ESC immediately.
    try { window.parent.focus(); } catch (_) {}
  }
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
      window.parent.postMessage({type: 'kensa-close-image'}, '*');
    }
  });
</script>
</body>
</html>"""
    }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
