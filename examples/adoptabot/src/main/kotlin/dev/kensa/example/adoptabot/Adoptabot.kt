package dev.kensa.example.adoptabot

import dev.kensa.example.adoptabot.AdoptionStatus.Adopted
import dev.kensa.example.adoptabot.AdoptionStatus.Available
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
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
 * @property shelterId The shelter the robot currently lives in
 */
data class Robot(val id: String, val name: String, val status: AdoptionStatus, val shelterId: String)

/**
 * Represents a shelter that houses robots awaiting adoption.
 */
data class Shelter(val id: String, val name: String, val location: String)

/**
 * Body of a PATCH /adopt/{id} request carrying the prospective adopter's details.
 */
data class AdoptionRequest(val adopterName: String, val adopterEmail: String)

/**
 * Confirmation returned when an adoption succeeds.
 */
data class AdoptionConfirmation(
    val reference: String,
    val robot: String,
    val adopter: String,
    val shelter: String,
)

/**
 * Body of a POST /adopters request registering a new adopter.
 */
data class AdopterRegistration(val name: String, val email: String)

/**
 * A registered adopter, returned from POST /adopters.
 */
data class Member(val memberId: String, val name: String, val email: String)

/**
 * In-memory storage for the robots in the system. Replaced with a database in a real application.
 */
var robots: MutableList<Robot> = mutableListOf()

/**
 * In-memory storage for the shelters in the system.
 */
var shelters: MutableList<Shelter> = mutableListOf()

/**
 * Path lens for extracting the robot ID from the request URL.
 */
val robotIdLens = Path.of("id")

/**
 * Derives the initials of a person's name, e.g. "Ada Lovelace" -> "AL".
 * Shared by the service and the test fixtures so derived values always agree.
 */
fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }

/**
 * Derives a stable adoption reference from a robot id and adopter name, e.g. "ADOPT-1-AL".
 */
fun adoptionReference(robotId: String, adopterName: String): String = "ADOPT-$robotId-${initialsOf(adopterName)}"

/**
 * Derives a contact email from an adopter's name, e.g. "Ada Lovelace" -> "ada.lovelace@adoptabot.test".
 */
fun adopterEmail(name: String): String = "${name.lowercase().replace(" ", ".")}@adoptabot.test"

/**
 * Derives a membership id from an adopter's name, e.g. "Ada Lovelace" -> "MEMBER-AL".
 */
fun membershipId(name: String): String = "MEMBER-${initialsOf(name)}"

private val robotListLens = Body.auto<List<Robot>>().toLens()
private val shelterListLens = Body.auto<List<Shelter>>().toLens()
private val adoptionRequestLens = Body.auto<AdoptionRequest>().toLens()
private val confirmationLens = Body.auto<AdoptionConfirmation>().toLens()
private val registrationLens = Body.auto<AdopterRegistration>().toLens()
private val memberLens = Body.auto<Member>().toLens()
private val shelterPathLens = Path.of("shelterId")

/**
 * Creates an HTTP handler for the robot adoption service.
 *
 * Endpoints:
 * - GET   /robots                       — available robots across all shelters
 * - GET   /shelters                     — the known shelters
 * - GET   /shelters/{shelterId}/robots  — available robots at a given shelter
 * - PATCH /adopt/{id}                   — adopt a robot; an adopter body returns a confirmation
 * - POST  /adopters                     — register an adopter, returning a membership
 */
fun adoptionService(): HttpHandler = routes(
    "/robots" bind GET to { _: Request ->
        Response(OK).with(robotListLens of robots.filter { it.status == Available })
    },
    "/shelters" bind GET to { _: Request ->
        Response(OK).with(shelterListLens of shelters)
    },
    "/shelters/{shelterId}/robots" bind GET to { req: Request ->
        val shelterId = shelterPathLens(req)
        Response(OK).with(robotListLens of robots.filter { it.status == Available && it.shelterId == shelterId })
    },
    "/adopt/{id}" bind PATCH to { req: Request ->
        val id = robotIdLens(req)
        val robotIndex = robots.indexOfFirst { it.id == id }
        if (robotIndex != -1 && robots[robotIndex].status == Available) {
            val adoptedRobot = robots[robotIndex].copy(status = Adopted)
            robots[robotIndex] = adoptedRobot
            if (req.bodyString().isBlank()) {
                Response(OK).body("""{ "message" : "Robot '${adoptedRobot.name}' adopted successfully!"}""")
            } else {
                val request = adoptionRequestLens(req)
                val shelterName = shelters.firstOrNull { it.id == adoptedRobot.shelterId }?.name ?: "Unknown"
                Response(OK).with(
                    confirmationLens of AdoptionConfirmation(
                        reference = adoptionReference(adoptedRobot.id, request.adopterName),
                        robot = adoptedRobot.name,
                        adopter = request.adopterName,
                        shelter = shelterName,
                    )
                )
            }
        } else {
            Response(BAD_REQUEST).body("""{ "message" : "Robot not available for adoption"}""")
        }
    },
    "/adopters" bind POST to { req: Request ->
        val registration = registrationLens(req)
        Response(OK).with(
            memberLens of Member(membershipId(registration.name), registration.name, registration.email)
        )
    },
)
