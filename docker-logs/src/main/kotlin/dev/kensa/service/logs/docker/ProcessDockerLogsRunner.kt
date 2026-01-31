package dev.kensa.service.logs.docker

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8

class ProcessDockerLogsRunner(
    private val dockerCmd: String = "docker"
) : DockerLogsRunner {

    override fun logs(container: String): Sequence<String> {
        val pb = ProcessBuilder(dockerCmd, "logs", container)
        pb.redirectErrorStream(true)

        val process = pb.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream, UTF_8))

        return sequence {
            reader.use { r ->
                while (true) {
                    val line = r.readLine() ?: break
                    yield(line)
                }
            }

            val exit = process.waitFor()
            if (exit != 0) {
                throw IllegalStateException("docker logs failed for container [$container] (exit code $exit)")
            }
        }
    }
}