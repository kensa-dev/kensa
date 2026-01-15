package dev.kensa.example.adoptabot

import dev.kensa.Kensa.konfigure
import dev.kensa.junit.KensaExtension
import dev.kensa.withRenderers
import org.http4k.client.OkHttp
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import java.net.ServerSocket
import kotlin.io.path.Path

/**
 * JUnit 5 extension for setting up and tearing down the Adoptabot test environment.
 * 
 * This extension:
 * 1. Configures Kensa with custom renderers
 * 2. Starts an HTTP server for the adoption service before all tests
 * 3. Stops the server after all tests
 * 
 * It also provides a client for making HTTP requests to the server and the port
 * on which the server is running.
 */
@ExtendWith(KensaExtension::class)
class AdoptabotExtension : BeforeAllCallback, AfterAllCallback {

    init {
        konfigure {
            outputDir = Path("${System.getProperty("user.dir")}/build/kensa-output")
            withRenderers {
                interactionRenderer(ResponseRenderer)
            }
        }
    }

    /**
     * Starts the HTTP server before all tests.
     */
    override fun beforeAll(context: ExtensionContext) {
        server.start()
    }

    /**
     * Stops the HTTP server after all tests.
     */
    override fun afterAll(context: ExtensionContext) {
        server.stop()
    }

    companion object {
        /**
         * HTTP client for making requests to the adoption service.
         */
        val client = OkHttp()

        /**
         * The port on which the adoption service is running.
         */
        val port = findAvailablePort()

        /**
         * The HTTP server for the adoption service.
         */
        private val server = adoptionService().asServer(SunHttp(port))

        /**
         * Finds an available port for the HTTP server.
         * 
         * @return An available port number
         */
        private fun findAvailablePort(): Int {
            ServerSocket(0).use { socket ->
                return socket.localPort
            }
        }
    }
}
