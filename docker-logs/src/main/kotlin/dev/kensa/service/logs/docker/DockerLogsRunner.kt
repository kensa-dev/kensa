package dev.kensa.service.logs.docker

fun interface DockerLogsRunner {
    fun logs(container: String): Sequence<String>
}