package dev.kensa.tabs.logs

import dev.kensa.service.logs.LogQueryService
import dev.kensa.tabs.KensaTabContext
import dev.kensa.tabs.KensaTabRenderer

class LogsTabRenderer : KensaTabRenderer {
  override fun render(ctx: KensaTabContext): String? {
    val sourceId = ctx.sourceId.takeIf { it.isNotBlank() } ?: return null
    val svc = ctx.services.get(LogQueryService::class)

    val logs = ctx.invocationIdentifier
      ?.takeIf { it.isNotBlank() }
      ?.let { id -> svc.query(sourceId, id) }
      ?: svc.queryAll(sourceId)

    if (logs.isEmpty()) return null

    return buildString {
      logs.forEach { appendLine(it.text.trimEnd()) }
    }.trimEnd()
  }
}