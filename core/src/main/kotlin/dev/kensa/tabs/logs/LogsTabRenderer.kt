package dev.kensa.tabs.logs

import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord
import dev.kensa.tabs.KensaTabContext
import dev.kensa.tabs.KensaTabRenderer
import dev.kensa.tabs.TabContent

class LogsTabRenderer : KensaTabRenderer {
  /**
   * Inverts the default relationship, taking its text from [renderTab], which this renderer therefore
   * overrides rather than inheriting.
   */
  override fun render(ctx: KensaTabContext): String? = renderTab(ctx)?.text

  override fun renderTab(ctx: KensaTabContext): TabContent? {
    val sourceId = ctx.sourceId.takeIf { it.isNotBlank() } ?: return null
    val svc = ctx.services.get(LogQueryService::class)

    val logs = ctx.invocationIdentifier
      ?.takeIf { it.isNotBlank() }
      ?.let { id -> svc.query(sourceId, id) }
      ?: svc.queryAll(sourceId)

    if (logs.isEmpty()) return TabContent(text = null, entries = 0)

    return TabContent(text = textOf(logs), entries = logs.size, records = logs)
  }

  private fun textOf(logs: List<LogRecord>): String =
    buildString {
      logs.forEach { appendLine(it.text.trimEnd()) }
    }.trimEnd()
}
