package dev.kensa.output.unfurl

// A page whose only job is to be fetched by a link unfurler: the Open Graph
// tags carry the card, the refresh sends a person straight on to the embed,
// and the link is there for a browser that ignores the refresh. Written as
// well-formed XHTML so any parser reads it; newlines in the description are
// character references because attribute newlines are otherwise folded.
internal fun unfurlPage(title: String, siteName: String, description: String, url: String, iconUrl: String): String {
    val t = escape(title)
    val u = escape(url)
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="utf-8"/>
        <title>$t</title>
        <meta name="robots" content="noindex"/>
        <link rel="icon" type="image/png" href="${escape(iconUrl)}"/>
        <meta property="og:type" content="article"/>
        <meta property="og:site_name" content="${escape(siteName)}"/>
        <meta property="og:title" content="$t"/>
        <meta property="og:description" content="${escape(description)}"/>
        <meta property="og:url" content="$u"/>
        <meta http-equiv="refresh" content="0; url=$u"/>
        </head>
        <body>
        <p><a href="$u">$t</a></p>
        </body>
        </html>
    """.trimIndent()
}

private fun escape(text: String): String = buildString(text.length) {
    text.forEach {
        when (it) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            '\n' -> append("&#10;")
            else -> append(it)
        }
    }
}
