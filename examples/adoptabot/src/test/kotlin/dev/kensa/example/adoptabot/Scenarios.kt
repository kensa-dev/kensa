package dev.kensa.example.adoptabot

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.kensa.Action
import dev.kensa.GivensContext

private val prettyMapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()

/**
 * Seeds the service with the shared shelter and robot catalogues. Only the function name is
 * rendered in the report, so the lambda body stays out of the test sentence.
 */
fun theAdoptionCentreIsStocked() = Action<GivensContext> {
    shelters = ArrayList(shelterCatalogue)
    robots = ArrayList(robotCatalogue)
}

/**
 * Pretty-printed JSON body for a PATCH /adopt/{id} request carrying adopter details.
 */
fun adoptionRequestBody(adopterName: String, adopterEmail: String): String =
    prettyMapper.writeValueAsString(AdoptionRequest(adopterName, adopterEmail))

/**
 * Pretty-printed JSON body for a POST /adopters registration request.
 */
fun adopterRegistrationBody(adopterName: String, adopterEmail: String): String =
    prettyMapper.writeValueAsString(AdopterRegistration(adopterName, adopterEmail))
