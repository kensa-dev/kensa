package dev.kensa.example.adoptabot

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.kensa.*
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.client
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.port
import dev.kensa.example.adoptabot.AdoptabotFixtures.RobotNameFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.ShelterIdFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.ShelterNameFx
import dev.kensa.example.adoptabot.AdoptabotParty.AdoptionService
import dev.kensa.example.adoptabot.AdoptabotParty.Client
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.render.Language.Json
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.util.Attributes
import io.kotest.matchers.collections.shouldContain
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Browsing shelters and the robots they house. Shelter names and robot names recur here and in the
 * adoption flows, giving the suite-wide search cross-test occurrences to follow.
 */
@ExtendWith(AdoptabotExtension::class)
@Notes("Lists the shelters and the available robots housed at a given shelter.")
class ShelterTest : KensaTest, WithKotest {

    private lateinit var response: Response

    @Test
    fun canBrowseTheShelters() {
        given(theAdoptionCentreIsStocked())

        whenever(aClientRequestsTheShelters())

        then(theResponse(), shouldHaveStatus(OK))
        and(theListedShelterNames()) { shouldContain(fixtures(ShelterNameFx)) }
    }

    @Test
    fun canListTheRobotsHousedAtAShelter() {
        given(theAdoptionCentreIsStocked())

        whenever(aClientRequestsTheRobotsAt(fixtures(ShelterIdFx)))

        then(theResponse(), shouldHaveStatus(OK))
        and(theListedRobotNames()) { shouldContain(fixtures(RobotNameFx)) }
    }

    private fun aClientRequestsTheShelters() = Action<ActionContext> { (_, interactions) ->
        val request = Request(GET, "http://localhost:$port/shelters")

        interactions.capture(from(Client).to(AdoptionService).with(request.uri.toString(), "Shelters Request"))

        response = client(request)

        interactions.capture(
            from(AdoptionService).to(Client)
                .with(response, "Shelters Response")
                .with(Attributes.of("language", Json))
        )
    }

    private fun aClientRequestsTheRobotsAt(shelterId: String) = Action<ActionContext> { (_, interactions) ->
        val request = Request(GET, "http://localhost:$port/shelters/$shelterId/robots")

        interactions.capture(from(Client).to(AdoptionService).with(request.uri.toString(), "Shelter Robots Request"))

        response = client(request)

        interactions.capture(
            from(AdoptionService).to(Client)
                .with(response, "Shelter Robots Response")
                .with(Attributes.of("language", Json))
        )
    }

    private fun theResponse() = StateCollector { response }

    private fun theListedShelterNames() = StateCollector {
        jacksonObjectMapper().readValue(response.bodyString(), object : TypeReference<List<Shelter>>() {}).map { it.name }
    }

    private fun theListedRobotNames() = StateCollector {
        jacksonObjectMapper().readValue(response.bodyString(), object : TypeReference<List<Robot>>() {}).map { it.name }
    }

    private fun shouldHaveStatus(expected: Status) = haveStatus(expected)
}
