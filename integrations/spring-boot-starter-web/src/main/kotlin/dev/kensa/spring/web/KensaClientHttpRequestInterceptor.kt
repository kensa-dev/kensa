package dev.kensa.spring.web

import dev.kensa.context.TestContextHolder
import dev.kensa.state.CapturedInteractionBuilder
import dev.kensa.state.CapturedInteractions
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class KensaClientHttpRequestInterceptor(
    private val client: HttpEndpoint = HttpEndpoint.Client,
    private val server: HttpEndpoint = HttpEndpoint.Server,
) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        captureInto { interactions ->
            CapturedInteractionBuilder.from(client)
                .to(server)
                .with(
                    HttpCapturedRequest(
                        method = request.method.name(),
                        uri = request.uri.toString(),
                        headers = request.headers.mapValues { it.value.toList() },
                        body = body.takeIf { it.isNotEmpty() }?.toString(Charsets.UTF_8),
                    ),
                    "HTTP ${request.method.name()} ${request.uri}",
                )
                .applyTo(interactions)
        }

        val response = execution.execute(request, body)

        captureInto { interactions ->
            CapturedInteractionBuilder.from(server)
                .to(client)
                .with(
                    HttpCapturedResponse(
                        status = response.statusCode.value(),
                        headers = response.headers.mapValues { it.value.toList() },
                        body = null,
                    ),
                    "HTTP ${response.statusCode.value()}",
                )
                .applyTo(interactions)
        }

        return response
    }

    private inline fun captureInto(block: (CapturedInteractions) -> Unit) {
        val ctx = runCatching { TestContextHolder.testContext() }.getOrNull() ?: return
        block(ctx.interactions)
    }
}
