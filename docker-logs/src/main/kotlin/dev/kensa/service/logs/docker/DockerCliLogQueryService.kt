package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord

class DockerCliLogQueryService(
    private val sources: List<DockerSource>,
    private val idPattern: Regex,
    private val runner: DockerLogsRunner = ProcessDockerLogsRunner(),
    delimiterLine: String,
) : LogQueryService {

    data class DockerSource(val id: String, val container: String)

    private val delimiterRegex: Regex = Regex("^\\s*${Regex.escape(delimiterLine.trim())}.*$")

    private val index: Map<String, Map<String, List<LogRecord>>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> = index[sourceId]?.get(identifier) ?: emptyList()

    private fun buildIndex(): Map<String, Map<String, List<LogRecord>>> =
        buildMap<String, Map<String, MutableList<LogRecord>>> {

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
                            if (delimiterRegex.matches(line)) {
                                if (isInBlock) flushBlock() else {
                                    isInBlock = true
                                    appendLine(line)
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

    private fun extractId(blockText: String): String? =
        blockText.lineSequence()
            .firstNotNullOfOrNull { line ->
                idPattern.matchEntire(line)?.groupValues?.getOrNull(1)?.trim()?.takeIf(String::isNotBlank)
            }
}