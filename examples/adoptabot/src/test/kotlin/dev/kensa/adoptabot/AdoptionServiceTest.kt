package dev.kensa.adoptabot

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.kensa.*
import dev.kensa.adoptabot.AdoptabotExtension.Companion.client
import dev.kensa.adoptabot.AdoptabotExtension.Companion.port
import dev.kensa.adoptabot.AdoptabotParty.AdoptionService
import dev.kensa.adoptabot.AdoptabotParty.Client
import dev.kensa.adoptabot.AdoptionStatus.Adopted
import dev.kensa.adoptabot.AdoptionStatus.Available
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.render.Language.Json
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.util.Attributes
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.and
import io.kotest.matchers.neverNullMatcher
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * BDD tests for the robot adoption service using Kensa.
 * 
 * This test class demonstrates how to use Kensa for BDD testing with the
 * Given-When-Then pattern. It tests the functionality of the robot adoption
 * service, including checking availability and adopting robots.
 */
@ExtendWith(AdoptabotExtension::class)
class AdoptionServiceTest : KensaTest, WithKotest {

    /**
     * Holds the state for the test, including the response and chosen robots.
     * The @RenderedValue annotation makes this value renderable in the test reports.
     */
    @RenderedValue
    private lateinit var holder: Holder

    /**
     * Initializes the holder before each test.
     */
    @BeforeEach
    fun before() {
        holder = Holder()
    }

    /**
     * Tests that a client can check the availability of robots.
     * 
     * This test:
     * 1. Sets up some available robots
     * 2. Makes a request to check availability
     * 3. Verifies that the response has a 200 OK status and contains the available robots
     */
    @Test
    fun canCheckAvailabilityOfRobots() {
        given(someAvailableRobotsExist())

        whenever(aClientRequestsAvailability())

        then(
            theResponse(), shouldHaveStatus(OK)
                .and(aBodyContaining(theAvailableRobots))
        )
    }

    /**
     * Tests that a client can adopt an available robot.
     * 
     * This test:
     * 1. Sets up some available robots
     * 2. Makes a request to adopt an available robot
     * 3. Verifies that the response has a 200 OK status
     */
    @Test
    fun canAdoptAnAvailableRobot() {
        given(someAvailableRobotsExist())

        whenever(aClientRequestsToAdopt(holder.chosenAvailableRobot))

        then(theResponse(), shouldHaveStatus(OK))
    }

    /**
     * Tests that a client cannot adopt an unavailable robot.
     * 
     * This test:
     * 1. Sets up some available robots
     * 2. Makes a request to adopt an unavailable robot
     * 3. Verifies that the response has a 400 BAD REQUEST status
     */
    @Test
    fun canNotAdoptAnUnavailableRobot() {
        given(someAvailableRobotsExist())

        whenever(aClientRequestsToAdopt(holder.chosenUnavailableRobot))

        then(theResponse(), shouldHaveStatus(BAD_REQUEST))
    }

    /**
     * Creates a matcher that checks if a response has the expected status.
     * 
     * @param expected The expected status
     * @return A matcher that checks if a response has the expected status
     */
    private fun shouldHaveStatus(expected: Status) = haveStatus(expected)

    /**
     * Creates a matcher that checks if a response body contains the expected robots.
     * 
     * @param expected The expected list of robots
     * @return A matcher that checks if a response body contains the expected robots
     */
    private fun aBodyContaining(expected: List<Robot>) = neverNullMatcher<Response> {
        val actual = jacksonObjectMapper().readValue(it.bodyString(), object : TypeReference<List<Robot>>() {})

        MatcherResult(
            actual == expected,
            { "Expected $expected but got $actual" },
            { "Actual list did not match expected list." }
        )
    }

    /**
     * Creates an action that sets up some available robots for testing.
     * 
     * @return An action that sets up some available robots
     */
    private fun someAvailableRobotsExist() = Action<GivensContext> {
        robots = ArrayList(allRobots)
    }

    /**
     * Creates an action that makes a request to check the availability of robots.
     * 
     * This action:
     * 1. Creates a GET request to the /robots endpoint
     * 2. Captures the interaction from the client to the service
     * 3. Makes the request and stores the response
     * 4. Captures the interaction from the service to the client
     * 
     * @return An action that makes a request to check availability
     */
    private fun aClientRequestsAvailability() = Action<ActionContext> { (_, interactions) ->
        val request = Request(GET, "http://localhost:$port/robots")

        interactions.capture(
            from(Client)
                .to(AdoptionService)
                .with(request.uri.toString(), "Availability Request")
        )

        holder.response = client(request)

        interactions.capture(
            from(AdoptionService)
                .to(Client)
                .with(holder.response, "Availability Response")
                .with(Attributes.of("language", Json))
        )
    }

    /**
     * Creates an action that makes a request to adopt a robot.
     * 
     * This action:
     * 1. Creates a PATCH request to the /adopt/{id} endpoint
     * 2. Captures the interaction from the client to the service
     * 3. Makes the request and stores the response
     * 4. Captures the interaction from the service to the client
     * 
     * @param robot The robot to adopt
     * @return An action that makes a request to adopt a robot
     */
    private fun aClientRequestsToAdopt(robot: Robot) = Action<ActionContext> { (_, interactions, outputs) ->
        val request = Request(PATCH, "http://localhost:$port/adopt/${robot.id}")

        outputs.put("robot", robot)
        interactions.capture(
            from(Client)
                .to(AdoptionService)
                .with(request.uri.toString(), "Adoption Request")
        )

        holder.response = client(request)

        interactions.capture(
            from(AdoptionService)
                .to(Client)
                .with(holder.response, "Adoption Response")
                .with(Attributes.of("language", Json))
        )
    }

    /**
     * Creates a state collector that returns the response from the holder.
     * 
     * @return A state collector that returns the response
     */
    private fun theResponse() = StateCollector { holder.response }

    /**
     * Holds the state for the test, including the response and chosen robots.
     */
    private class Holder {
        /**
         * A randomly chosen available robot for testing adoption.
         */
        val chosenAvailableRobot: Robot = theAvailableRobots.random()

        /**
         * A randomly chosen unavailable robot for testing adoption failure.
         */
        val chosenUnavailableRobot: Robot = theUnavailableRobots.random()

        /**
         * The response from the most recent request.
         */
        lateinit var response: Response
    }

    companion object {
        private val allRobots = listOf(
            Robot("1", "Bolt", Available),
            Robot("2", "Chip", Available),
            Robot("3", "Sparks", Adopted),
            Robot("4", "Buzz", Adopted),
            Robot("5", "Gizmo", Available),
            Robot("6", "Cogs", Available),
        )

        private val theUnavailableRobots = allRobots.filter { it.status != Available }

        private val theAvailableRobots = allRobots.filter { it.status == Available }
    }
}
