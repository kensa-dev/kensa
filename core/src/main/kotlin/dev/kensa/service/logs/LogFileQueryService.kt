package dev.kensa.service.logs

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class LogFileQueryService(
    private val sources: List<FileSource>,
    private val delimiterLine: String,
    private val idField: String
) : LogQueryService {

    data class FileSource(val id: String, val path: Path)

    private val index: Map<String, List<LogRecord>> by lazy { buildIndex() }

    override fun query(identifier: String): List<LogRecord> = index[identifier].orEmpty()

    private fun buildIndex(): Map<String, List<LogRecord>> =
        buildMap<String, MutableList<LogRecord>> {

            val normalizedDelimiterLine = delimiterLine.trim()

            sources.forEach { source ->
                if (!Files.exists(source.path)) return@forEach

                Files.newBufferedReader(source.path, StandardCharsets.UTF_8).useLines { lines ->
                    val current = StringBuilder()
                    var isInBlock = false

                    fun flushBlock() {
                        val blockText = current.toString().trim()
                        current.setLength(0)
                        isInBlock = false
                        if (blockText.isBlank()) return

                        val id = extractId(blockText) ?: return
                        getOrPut(id) { mutableListOf() }
                            .add(LogRecord(sourceId = source.id, identifier = id, text = blockText))
                    }

                    for (line in lines) {
                        if (line.trim() == normalizedDelimiterLine) {
                            if (isInBlock) flushBlock() else {
                                isInBlock = true
                                current.appendLine(delimiterLine)
                            }
                            continue
                        }

                        if (isInBlock) current.appendLine(line)
                    }

                    if (isInBlock) flushBlock()
                }
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