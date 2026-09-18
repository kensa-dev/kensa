package dev.kensa.tabs

import dev.kensa.service.logs.LogRecord

/**
 * Content produced by a [KensaTabRenderer] for a single invocation.
 *
 * [text] is the rendered tab body, or null when there is nothing to display.
 * [entries] is the number of underlying entries the tab represents, recorded even when it is zero.
 * [records] are the raw log records backing the tab, written alongside the rendered text.
 */
data class TabContent(
    val text: String?,
    val entries: Int? = null,
    val records: List<LogRecord>? = null
)
