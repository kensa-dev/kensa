package dev.kensa.service.logs.docker

import dev.kensa.service.logs.LogQueryServiceRegistry

fun LogQueryServiceRegistry.dockerCli(
    id: String,
    container: String,
    idPattern: Regex,
    delimiterLine: String,
) {
    register(id) { sourceId ->
        DockerCliLogQueryService(
            sources = listOf(DockerCliLogQueryService.DockerSource(id = sourceId, container = container)),
            idPattern = idPattern,
            delimiterLine = delimiterLine,
        )
    }
}