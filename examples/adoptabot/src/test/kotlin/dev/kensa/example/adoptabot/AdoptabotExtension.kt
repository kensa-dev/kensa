package dev.kensa.example.adoptabot

import dev.kensa.Kensa.konfigure
import dev.kensa.Tab
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.junit.KensaExtension
import dev.kensa.withRenderers
import org.http4k.client.OkHttp
import org.http4k.server.SunHttp
import org.http4k.server.asServer
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
class AdoptabotExtension : BeforeAllCallback, AutoCloseable {

    init {
        registerFixtures(AdoptabotFixtures)
        konfigure {
            titleText = "Adoptabot Acceptance Tests"
            outputDir = Path("${System.getProperty("user.dir")}/build/kensa-output")
            autoOpenTab = Tab.SequenceDiagram
            sourceLocations = listOf(Path("src/test/kotlin"))
            withRenderers {
                interactionRenderer(ResponseRenderer)
            }
        }
    }

    /**
     * Starts the HTTP server before all tests.
     */
    override fun beforeAll(context: ExtensionContext) {
        val rootStore = context.root.getStore(ExtensionContext.Namespace.GLOBAL)

        rootStore.computeIfAbsent("ADOPTABOT_SERVER_HOLDER") {
            server.start()
            this
        }
    }

    /**
     * Stops the HTTP server after all tests.
     */
    override fun close() {
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

/**
 * Deterministic, distinctive fixture values, deliberately shared across multiple test classes so
 * the suite-wide fixture search has real cross-test occurrences to find. Derived values reuse the
 * same helpers the service uses ([adoptionReference], [adopterEmail], [membershipId]) so they
 * always agree with the rendered payloads.
 */
object AdoptabotFixtures : FixtureContainer {
    val RobotNameFx = fixture("RobotName") { "Bolt" }
    val RobotIdFx = fixture("RobotId", RobotNameFx) { name -> robotNamed(name).id }
    val AdoptionPathFx = fixture("AdoptionPath", RobotIdFx) { id -> "/adopt/$id" }

    val ShelterNameFx = fixture("ShelterName") { "Old Foundry" }
    val ShelterIdFx = fixture("ShelterId", ShelterNameFx) { name -> shelterNamed(name).id }

    val AdopterNameFx = fixture("AdopterName") { "Ada Lovelace" }
    val AdopterEmailFx = fixture("AdopterEmail", AdopterNameFx) { name -> adopterEmail(name) }
    val MembershipIdFx = fixture("MembershipId", AdopterNameFx) { name -> membershipId(name) }

    val AdoptionReferenceFx = fixture("AdoptionReference", RobotIdFx, AdopterNameFx) { id, name -> adoptionReference(id, name) }
}
