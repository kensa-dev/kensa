package dev.kensa.tabs.logs

import dev.kensa.service.logs.LogQueryService
import dev.kensa.tabs.KensaTabContext
import dev.kensa.tabs.KensaTabRenderer

class LogsTabRenderer : KensaTabRenderer {
  override fun render(ctx: KensaTabContext): String? {
    val id = ctx.invocationIdentifier?.takeIf { it.isNotBlank() } ?: return null
    val logs = ctx.services.get(LogQueryService::class).query(id)
    if (logs.isEmpty()) return null

    return buildString {
      logs.groupBy { it.sourceId }.forEach { (sourceId, records) ->
        appendLine(":: Source: $sourceId ::")
        records.forEach {
          appendLine(it.text.trimEnd())
          appendLine()
        }
      }
    }.trimEnd()
  }
}