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
            val ariaLabel = if (label.isNotBlank()) "Expand screenshot: ${escapeHtml(label)}" else "Expand screenshot"
            val caption = if (label.isNotBlank()) {
                """    <figcaption>${escapeHtml(label)}</figcaption>"""
            } else ""
            """  <figure class="shot">
    <button type="button" class="thumb" onclick="expand(this.querySelector('img'))" aria-label="$ariaLabel">
      <img src="${escapeHtml(src)}" alt="${escapeHtml(label)}" loading="lazy" />
    </button>
$caption
  </figure>""".trimEnd()
        }
        return """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"/>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  :root {
    --bg: #fafafa;
    --fg: #333;
    --muted: #666;
    --card: #fff;
    --border: #e5e5e5;
    --accent: #2f8a64;
    --accent-soft: #b6d8c5;
  }
  :root.kensa-dark {
    --bg: #0f0f0f;
    --fg: #d4d4d4;
    --muted: #888;
    --card: #1a1a1a;
    --border: #2a2a2a;
  }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
    background: var(--bg);
    color: var(--fg);
    padding: 20px;
  }
  .grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 24px 18px;
  }
  .shot { display: flex; flex-direction: column; gap: 8px; min-width: 0; }
  .thumb {
    appearance: none;
    border: 1px solid var(--border);
    padding: 0;
    background: var(--card);
    border-radius: 6px;
    overflow: hidden;
    aspect-ratio: 4 / 3;
    cursor: zoom-in;
    transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
  }
  .thumb:hover {
    transform: translateY(-1px);
    border-color: var(--accent-soft);
    box-shadow: 0 6px 20px -10px rgba(0, 0, 0, 0.18);
  }
  .thumb:focus-visible {
    outline: 2px solid var(--accent);
    outline-offset: 2px;
  }
  .thumb img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
  figcaption {
    font-size: 12px;
    line-height: 1.3;
    color: var(--muted);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
</style>
</head>
<body>
<div class="grid">
$sections
</div>
<script>
  function applyParentTheme() {
    try {
      var isDark = window.parent.document.documentElement.classList.contains('dark');
      document.documentElement.classList.toggle('kensa-dark', isDark);
    } catch (_) { /* parent inaccessible — leave as light */ }
  }
  applyParentTheme();
  try {
    new MutationObserver(applyParentTheme).observe(
      window.parent.document.documentElement,
      { attributes: true, attributeFilter: ['class'] }
    );
  } catch (_) {}

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
