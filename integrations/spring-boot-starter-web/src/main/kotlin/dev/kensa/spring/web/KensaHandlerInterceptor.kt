package dev.kensa.spring.web

import dev.kensa.context.TestContextHolder
import dev.kensa.state.CapturedInteractionBuilder
import dev.kensa.state.CapturedInteractions
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

class KensaHandlerInterceptor(
    private val client: HttpEndpoint = HttpEndpoint.Client,
    private val server: HttpEndpoint = HttpEndpoint.Server,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        captureInto { interactions ->
            CapturedInteractionBuilder.from(client)
                .to(server)
                .with(request.toCaptured(), "HTTP ${request.method} ${request.requestURI}")
                .applyTo(interactions)
        }
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        captureInto { interactions ->
            CapturedInteractionBuilder.from(server)
                .to(client)
                .with(response.toCaptured(), "HTTP ${response.status}")
                .applyTo(interactions)
        }
    }

    private inline fun captureInto(block: (CapturedInteractions) -> Unit) {
        val ctx = runCatching { TestContextHolder.testContext() }.getOrNull() ?: return
        block(ctx.interactions)
    }

    private fun HttpServletRequest.toCaptured(): HttpCapturedRequest {
        val headers = headerNames.toList().associateWith { getHeaders(it).toList() }
        return HttpCapturedRequest(method, requestURI, headers, body = null)
    }

    private fun HttpServletResponse.toCaptured(): HttpCapturedResponse {
        val headers = headerNames.associateWith { getHeaders(it).toList() }
        return HttpCapturedResponse(status, headers, body = null)
    }
}
