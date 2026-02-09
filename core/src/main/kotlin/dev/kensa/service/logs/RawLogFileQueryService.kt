package dev.kensa.service.logs

import java.nio.charset.StandardCharsets.UTF_8
import kotlin.io.path.bufferedReader
import kotlin.io.path.notExists

class RawLogFileQueryService(
    private val sources: List<FileSource>,
    private val tailLines: Int? = null
) : LogQueryService {

    private val sourcesById = sources.associateBy { it.id }

    override fun query(sourceId: String, identifier: String): List<LogRecord> {
        val text = readText(sourceId) ?: return emptyList()
        val filtered = text.lineSequence().filter { it.contains(identifier) }.joinToString("\n").trim()
        return if (filtered.isBlank()) emptyList()
        else listOf(LogRecord(sourceId = sourceId, identifier = identifier, text = filtered))
    }

    override fun queryAll(sourceId: String): List<LogRecord> {
        val text = readText(sourceId) ?: return emptyList()
        return listOf(LogRecord(sourceId = sourceId, identifier = "", text = text))
    }

    private fun readText(sourceId: String): String? {
        val src = sourcesById[sourceId] ?: return null
        if (src.path.notExists()) return null

        tailLines?.let { n ->
            if (n <= 0) return ""
            val deque = ArrayDeque<String>(n)
            src.path.bufferedReader(UTF_8).useLines { lines ->
                lines.forEach { line ->
                    if (deque.size == n) deque.removeFirst()
                    deque.addLast(line)
                }
            }
            return deque.joinToString("\n").trim()
        }

        return src.path.bufferedReader(UTF_8).use { it.readText() }.trim()
    }
}