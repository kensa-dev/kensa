package dev.kensa.service.logs

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.notExists

class LogFileQueryService(
    private val sources: List<FileSource>,
    private val idPattern: Regex,
    delimiterLine: String
) : LogQueryService {

    data class FileSource(val id: String, val path: Path)

    private val delimiterRegex: Regex = Regex("^\\s*${Regex.escape(delimiterLine.trim())}.*$")

    private val index: Map<String, Map<String, List<LogRecord>>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> =
        index[sourceId]?.get(identifier).orEmpty()

    private fun buildIndex(): Map<String, Map<String, List<LogRecord>>> =
        buildMap<String, Map<String, List<LogRecord>>> {
            sources.forEach { source ->
                if (source.path.notExists()) return@forEach

                val recordsById = buildMap<String, MutableList<LogRecord>> {
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
                                if (isInBlock) {
                                    flushBlock()
                                } else {
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

                put(source.id, recordsById.mapValues { (_, v) -> v.toList() })
            }
        }

    private fun extractId(blockText: String): String? =
        blockText.lineSequence()
            .firstNotNullOfOrNull { line ->
                idPattern.matchEntire(line)?.groupValues?.getOrNull(1)?.trim()?.takeIf(String::isNotBlank)
            }
}