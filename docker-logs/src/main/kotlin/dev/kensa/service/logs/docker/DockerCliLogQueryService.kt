package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogPatterns
import dev.kensa.service.logs.LogQueryService
import dev.kensa.service.logs.LogRecord
import dev.kensa.service.logs.LogSource

class DockerCliLogQueryService(
    private val sources: List<DockerSource>,
    private val idPattern: Regex,
    private val delimiterRegex: Regex,
    private val runner: DockerLogsRunner = ProcessDockerLogsRunner(),
) : LogQueryService {

    constructor(sources: List<DockerSource>, idPattern: Regex, delimiterLine: String, runner: DockerLogsRunner = ProcessDockerLogsRunner()) :
            this(sources, idPattern, LogPatterns.delimiterPrefix(delimiterLine), runner)

    data class DockerSource(val id: String, val container: String)

    private val index: Map<String, Map<String, List<LogRecord>>> by lazy { buildIndex() }

    override fun query(sourceId: String, identifier: String): List<LogRecord> = index[sourceId]?.get(identifier).orEmpty()

    override fun queryAll(sourceId: String): List<LogRecord> =
        index[sourceId]
            ?.values
            ?.flatten()
            .orEmpty()

    override fun sources(): List<LogSource> =
        sources.map { LogSource(id = it.id, file = it.container, present = true) }

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
                                if (isInBlock) flushBlock()
                                isInBlock = true
                                appendLine(line)
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