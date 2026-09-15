package dev.kensa.output.unfurl

import java.net.URL
import java.net.URLEncoder

// The report's own embed link for a class or method, as the UI's Copy embed
// link builds it: the source id is what the UI prefixes to every class id,
// "default" for a report served without a manifest. A base ending in "/" is
// a directory, so index.html is appended to keep the hash on the report page.
internal fun embedUrl(linkBaseUrl: URL, sourceId: String, className: String, method: String?): String {
    val base = linkBaseUrl.toString().let { if (it.endsWith("/")) "${it}index.html" else it }
    val query = method?.let { "?method=${URLEncoder.encode(it, Charsets.UTF_8)}" } ?: ""
    return "$base#/embed/$sourceId::$className$query"
}

// The report's favicon, absolute because an unfurler resolves the icon against
// the page it fetched, and the page sits two directories below the report.
internal fun iconUrl(linkBaseUrl: URL): String {
    val base = linkBaseUrl.toString()
    return "${base.substring(0, base.lastIndexOf('/') + 1)}favicon.png"
}
