package dev.kensa.spring.web

import dev.kensa.context.TestContextHolder
import dev.kensa.state.CapturedInteractionBuilder
import dev.kensa.state.CapturedInteractions
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

class KensaWebClientFilter(
    private val client: HttpEndpoint = HttpEndpoint.Client,
    private val server: HttpEndpoint = HttpEndpoint.Server,
) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        captureRequest(request)
        return next.exchange(request).doOnNext(::captureResponse)
    }

    private fun captureRequest(request: ClientRequest) = captureInto { interactions ->
        CapturedInteractionBuilder.from(client)
            .to(server)
            .with(
                HttpCapturedRequest(
                    method = request.method().name(),
                    uri = request.url().toString(),
                    headers = request.headers().mapValues { it.value.toList() },
                    body = null,
                ),
                "HTTP ${request.method().name()} ${request.url()}",
            )
            .applyTo(interactions)
    }

    private fun captureResponse(response: ClientResponse) = captureInto { interactions ->
        CapturedInteractionBuilder.from(server)
            .to(client)
            .with(
                HttpCapturedResponse(
                    status = response.statusCode().value(),
                    headers = response.headers().asHttpHeaders().mapValues { it.value.toList() },
                    body = null,
                ),
                "HTTP ${response.statusCode().value()}",
            )
            .applyTo(interactions)
    }

    private inline fun captureInto(block: (CapturedInteractions) -> Unit) {
        val ctx = runCatching { TestContextHolder.testContext() }.getOrNull() ?: return
        block(ctx.interactions)
    }
}
