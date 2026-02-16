package dev.kensa.service.logs

import java.nio.charset.StandardCharsets.UTF_8
import kotlin.io.path.bufferedReader
import kotlin.io.path.notExists

class IndexedLogFileQueryService(
    private val source: FileSource,
    private val idPattern: Regex,
    delimiterLine: String
) : LogQueryService {

    private val delimiterRegex: Regex = LogPatterns.delimiterPrefix(delimiterLine)

    private val index: Map<String, List<LogRecord>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> {
        if (sourceId != source.id) return emptyList()
        return index[identifier].orEmpty()
    }

    override fun queryAll(sourceId: String): List<LogRecord> {
        if (sourceId != source.id) return emptyList()
        return index.values.flatten()
    }

    private fun buildIndex(): Map<String, List<LogRecord>> {
        if (source.path.notExists()) return emptyMap()

        val blocksById = buildMap {
            source.path.bufferedReader(UTF_8).useLines { lines ->
                val buf = StringBuilder()
                var isInBlock = false

                fun flushBlock() {
                    val blockText = buf.toString().trim()
                    buf.setLength(0)
                    isInBlock = false
                    if (blockText.isBlank()) return

                    val id = extractId(blockText) ?: return
                    getOrPut(id) { mutableListOf() }
                        .add(LogRecord(sourceId = source.id, identifier = id, text = blockText))
                }

                for (line in lines) {
                    if (delimiterRegex.matches(line)) {
                        if (isInBlock) flushBlock() else {
                            isInBlock = true
                            buf.appendLine(line)
                        }
                        continue
                    }

                    if (isInBlock) buf.appendLine(line)
                }

                if (isInBlock) flushBlock()
            }
        }

        return blocksById.mapValues { (_, v) -> v.toList() }
    }

    private fun extractId(blockText: String): String? =
        blockText.lineSequence()
            .firstNotNullOfOrNull { line ->
                idPattern.matchEntire(line)?.groupValues?.getOrNull(1)?.trim()?.takeIf(String::isNotBlank)
            }
}