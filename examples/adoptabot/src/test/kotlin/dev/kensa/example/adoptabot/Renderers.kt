package dev.kensa.example.adoptabot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import dev.kensa.example.adoptabot.prettyPrintJson
import dev.kensa.render.InteractionRenderer
import dev.kensa.render.Language
import dev.kensa.render.RenderedAttributes
import dev.kensa.render.RenderedInteraction
import dev.kensa.util.Attributes
import dev.kensa.util.NamedValue
import org.http4k.core.Response

/**
 * Extension function to format a JSON string with proper indentation.
 * 
 * This function takes a JSON string and returns a formatted version with
 * proper indentation for better readability in test reports.
 * 
 * @return The formatted JSON string
 */
fun String.prettyPrintJson(): String {
    val mapper = ObjectMapper().enable(INDENT_OUTPUT)
    return mapper.writeValueAsString(mapper.readTree(this))
}

/**
 * Custom renderer for HTTP responses in Kensa BDD test reports.
 * 
 * This renderer formats the response body as JSON and extracts the status
 * and headers as attributes for better readability in test reports.
 */
object ResponseRenderer : InteractionRenderer<Response> {
    /**
     * Renders the response body as formatted JSON.
     * 
     * @param value The HTTP response to render
     * @param attributes Additional attributes for rendering
     * @return A list of rendered interactions
     */
    override fun render(value: Response, attributes: Attributes): List<RenderedInteraction> {
        return listOf(RenderedInteraction("Response Body", value.bodyString().prettyPrintJson(), Language.Json))
    }

    /**
     * Extracts and renders the status and headers from the HTTP response.
     * 
     * @param value The HTTP response to extract attributes from
     * @return A list of rendered attributes
     */
    override fun renderAttributes(value: Response): List<RenderedAttributes> {
        return listOf(
            RenderedAttributes(
                "Status", setOf(NamedValue("Status", value.status.code.toString())),
            ),
            RenderedAttributes(
                "Headers", value.headers.map { NamedValue(it.first, it.second) }.toSet()
            )
        )
    }
}
