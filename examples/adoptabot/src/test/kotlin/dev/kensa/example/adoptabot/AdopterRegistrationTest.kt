package dev.kensa.example.adoptabot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.kensa.*
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.client
import dev.kensa.example.adoptabot.AdoptabotExtension.Companion.port
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdopterEmailFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.AdopterNameFx
import dev.kensa.example.adoptabot.AdoptabotFixtures.MembershipIdFx
import dev.kensa.example.adoptabot.AdoptabotParty.AdoptionService
import dev.kensa.example.adoptabot.AdoptabotParty.Client
import dev.kensa.junit.KensaTest
import dev.kensa.kotest.WithKotest
import dev.kensa.render.Language.Json
import dev.kensa.state.CapturedInteractionBuilder.Companion.from
import dev.kensa.util.Attributes
import io.kotest.matchers.shouldBe
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.kotest.haveStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Registering an adopter before they adopt. The adopter name and email recur in the adoption flow,
 * so the same values are searchable across this test and the adoption tests.
 */
@ExtendWith(AdoptabotExtension::class)
@Notes("Registers a prospective adopter and returns their membership details.")
class AdopterRegistrationTest : KensaTest, WithKotest {

    private lateinit var response: Response

    @Test
    fun canRegisterAnAdopter() {
        whenever(anAdopterRegistersAs(fixtures(AdopterNameFx), fixtures(AdopterEmailFx)))

        then(theResponse(), shouldHaveStatus(OK))
        and(theMembershipId()) { shouldBe(fixtures(MembershipIdFx)) }
        and(theRegisteredEmail()) { shouldBe(fixtures(AdopterEmailFx)) }
    }

    private fun anAdopterRegistersAs(adopterName: String, adopterEmail: String) =
        Action<ActionContext> { (_, interactions) ->
            val body = adopterRegistrationBody(adopterName, adopterEmail)
            val request = Request(POST, "http://localhost:$port/adopters").body(body)

            interactions.capture(
                from(Client).to(AdoptionService)
                    .with(body, "Registration Request")
                    .with(Attributes.of("language", Json))
            )

            response = client(request)

            interactions.capture(
                from(AdoptionService).to(Client)
                    .with(response, "Registration Response")
                    .with(Attributes.of("language", Json))
            )
        }

    private fun member(): Member = jacksonObjectMapper().readValue(response.bodyString())

    private fun theResponse() = StateCollector { response }
    private fun theMembershipId() = StateCollector { member().memberId }
    private fun theRegisteredEmail() = StateCollector { member().email }

    private fun shouldHaveStatus(expected: Status) = haveStatus(expected)
}
