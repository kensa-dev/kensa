package dev.kensa.service.logs

import java.nio.charset.StandardCharsets.UTF_8
import kotlin.io.path.bufferedReader
import kotlin.io.path.notExists

class RawLogFileQueryService(
    private val source: FileSource,
    private val tailLines: Int? = null
) : LogQueryService {

    private val cachedText: String? by lazy { readTextInternal() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> {
        if (sourceId != source.id) return emptyList()

        val text = cachedText ?: return emptyList()
        val filtered = text.lineSequence().filter { it.contains(identifier) }.joinToString("\n").trim()

        return if (filtered.isBlank()) emptyList()
        else listOf(LogRecord(sourceId = sourceId, identifier = identifier, text = filtered))
    }

    override fun queryAll(sourceId: String): List<LogRecord> {
        if (sourceId != source.id) return emptyList()

        val text = cachedText ?: return emptyList()
        return listOf(LogRecord(sourceId = sourceId, identifier = "", text = text))
    }

    private fun readTextInternal(): String? {
        if (source.path.notExists()) return null

        tailLines?.let { n ->
            if (n <= 0) return ""
            val deque = ArrayDeque<String>(n)
            source.path.bufferedReader(UTF_8).useLines { lines ->
                lines.forEach { line ->
                    if (deque.size == n) deque.removeFirst()
                    deque.addLast(line)
                }
            }
            return deque.joinToString("\n").trim()
        }

        return source.path.bufferedReader(UTF_8).use { it.readText() }.trim()
    }
}