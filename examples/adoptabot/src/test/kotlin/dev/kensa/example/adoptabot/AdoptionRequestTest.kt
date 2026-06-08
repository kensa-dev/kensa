package dev.kensa.example.adoptabot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kensa.*
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.client
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.port
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdopterEmailFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdopterNameFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdoptionPathFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdoptionReferenceFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.RobotNameFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.ShelterNameFx
import dev.kensa.example.adoptabot.AdoptabotParty.AdoptionService
import dev.kensa.example.adoptabot.AdoptabotParty.Client
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.render.Language.Json
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.util.Attributes
import io.kotest.matchers.shouldBe
import org.http4k.core.Method.PATCH
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Adopting a robot on behalf of a named adopter. The adopter's details travel in the request body
 * and the confirmation echoes them back, so fixture values like the robot name, adopter name and
 * adoption reference appear in the rendered interaction payloads — and recur in other test classes.
 */
@ExtendWith(AdoptabotExtension::class)
@Notes("Drives the full adoption flow: an adopter's details are posted with the request and echoed in the confirmation.")
class AdoptionRequestTest : KensaTest, WithKotest {

    private lateinit var response: Response

    @Test
    fun canAdoptARobotOnBehalfOfAnAdopter() {
        given(theAdoptionCentreIsStocked())

        whenever(anAdopterRequestsToAdoptAt(fixtures(AdoptionPathFx), fixtures(AdopterNameFx), fixtures(AdopterEmailFx)))

        then(theResponse(), shouldHaveStatus(OK))
        and(theAdoptionReference()) { shouldBe(fixtures(AdoptionReferenceFx)) }
        and(theAdoptedRobot()) { shouldBe(fixtures(RobotNameFx)) }
        and(theConfirmedShelter()) { shouldBe(fixtures(ShelterNameFx)) }
    }

    private fun anAdopterRequestsToAdoptAt(path: String, adopterName: String, adopterEmail: String) =
        Action<ActionContext> { (_, interactions) ->
            val body = adoptionRequestBody(adopterName, adopterEmail)
            val request = Request(PATCH, "http://localhost:$port$path").body(body)

            interactions.capture(
                from(Client).to(AdoptionService)
                    .with(body, "Adoption Request")
                    .with(Attributes.of("language", Json))
            )

            response = client(request)

            interactions.capture(
                from(AdoptionService).to(Client)
                    .with(response, "Adoption Response")
                    .with(Attributes.of("language", Json))
            )
        }

    private fun confirmation(): AdoptionConfirmation = jacksonObjectMapper().readValue(response.bodyString())

    private fun theResponse() = StateCollector { response }
    private fun theAdoptionReference() = StateCollector { confirmation().reference }
    private fun theAdoptedRobot() = StateCollector { confirmation().robot }
    private fun theConfirmedShelter() = StateCollector { confirmation().shelter }

    private fun shouldHaveStatus(expected: Status) = haveStatus(expected)
}
