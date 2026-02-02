package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord

class DockerCliLogQueryService(
    private val sources: List<DockerSource>,
    private val delimiterLine: String,
    private val idField: String,
    private val runner: DockerLogsRunner = ProcessDockerLogsRunner()
) : LogQueryService {

    data class DockerSource(val id: String, val container: String)

    private val index: Map<String, Map<String, List<LogRecord>>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> = index[sourceId]?.get(identifier) ?: emptyList()

    private fun buildIndex(): Map<String, Map<String, List<LogRecord>>> =
        buildMap<String, Map<String, MutableList<LogRecord>>> {

            val normalizedDelimiterLine = delimiterLine.trim()

            sources.forEach { source ->

                val recordsById = buildMap {
                    val lines = runner.logs(source.container)
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