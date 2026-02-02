package dev.kensa.service.logs

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.notExists

class LogFileQueryService(
    private val sources: List<FileSource>,
    private val delimiterLine: String,
    private val idField: String
) : LogQueryService {

    data class FileSource(val id: String, val path: Path)

    private val index: Map<String, Map<String, List<LogRecord>>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> = index[sourceId]?.get(identifier) ?: emptyList()

    private fun buildIndex(): Map<String, Map<String, List<LogRecord>>> =
        buildMap<String, Map<String, MutableList<LogRecord>>> {

            val normalizedDelimiterLine = delimiterLine.trim()

            sources.forEach { source ->
                if (source.path.notExists()) return@forEach

                val recordsById = buildMap {
                    source.path.bufferedReader(UTF_8).useLines { lines ->
                        var isInBlock = false
                        buildString {

                            fun flushBlock() {
                                val blockText = toString().trim()
                                setLength(0)
                                isInBlock = false
                                if (blockText.isBlank()) return

                                val id = extractId(blockText) ?: return
                                getOrPut(id) { mutableListOf() }
                                    .add(LogRecord(sourceId = source.id, identifier = id, text = blockText))
                            }

                            for (line in lines) {
                                if (line.trim().startsWith(normalizedDelimiterLine)) {
                                    if (isInBlock) flushBlock() else {
                                        isInBlock = true
                                        appendLine(delimiterLine)
                                    }
                                    continue
                                }

                                if (isInBlock) appendLine(line)
                            }

                            if (isInBlock) flushBlock()
                        }
                    }
                }

                put(source.id, recordsById)
            }
        }

    private fun extractId(blockText: String): String? {
        val prefix = "$idField:"
        return blockText
            .lineSequence()
            .map(String::trim)
            .firstNotNullOfOrNull { line ->
                line
                    .takeIf { it.startsWith(prefix) }
                    ?.removePrefix(prefix)
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
    }
}