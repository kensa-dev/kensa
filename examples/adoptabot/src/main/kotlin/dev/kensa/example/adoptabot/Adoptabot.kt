package dev.kensa.example.adoptabot

import dev.kensa.example.adoptabot.AdoptionStatus.Adopted
import dev.kensa.example.adoptabot.AdoptionStatus.Available
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes

/**
 * Represents the adoption status of a robot.
 * A robot can be either Available for adoption or already Adopted.
 */
enum class AdoptionStatus {
    Available,
    Adopted
}

/**
 * Represents a robot in the adoption system.
 *
 * @property id The unique identifier of the robot
 * @property name The name of the robot
 * @property status The current adoption status of the robot
 */
data class Robot(val id: String, val name: String, val status: AdoptionStatus)

/**
 * In-memory storage for the list of robots in the system.
 * In a real application, this would be replaced with a database.
 */
var robots: MutableList<Robot> = mutableListOf()

/**
 * Path lens for extracting the robot ID from the request URL.
 */
val robotIdLens = Path.of("id")

/**
 * Creates an HTTP handler for the robot adoption service.
 * 
 * This service provides two endpoints:
 * - GET /robots - Returns a list of available robots
 * - PATCH /adopt/{id} - Adopts a robot by changing its status to Adopted
 *
 * @return An HttpHandler that handles requests to the adoption service
 */
fun adoptionService(): HttpHandler = routes(
    "/robots" bind GET to { _: Request ->
        val availableRobots = robots.filter { it.status == Available }
        Response(OK).with(Body.auto<List<Robot>>().toLens() of availableRobots)
    },
    "/adopt/{id}" bind PATCH to { req: Request ->
        val id = robotIdLens(req)
        val robotIndex = robots.indexOfFirst { it.id == id }
        if (robotIndex != -1 && robots[robotIndex].status == Available) {
            val updatedRobot = robots[robotIndex].copy(status = Adopted)
            robots[robotIndex] = updatedRobot
            Response(OK).body("""{ "message" : "Robot '${updatedRobot.name}' adopted successfully!"}""")
        } else {
            Response(BAD_REQUEST).body("""{ "message" : "Robot not available for adoption"}""")
        }
    }
)
