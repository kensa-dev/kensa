package dev.kensa.spring.web

import dev.kensa.context.TestContextHolder
import dev.kensa.spring.KensaTest
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus.OK
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@KensaTest
@Execution(SAME_THREAD)
class KensaWebClientFilterTest {

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    open class TestApp {

        @Bean
        open fun webClient(): WebClient =
            WebClient.builder()
                .filter(KensaWebClientFilter())
                .exchangeFunction { _ -> Mono.just(ClientResponse.create(OK).body("{}").build()) }
                .build()
    }

    @Autowired
    lateinit var webClient: WebClient

    @Test
    fun `captures outbound request and inbound response interactions for WebClient`() {
        webClient.get()
            .uri("https://api.example.com/users/7")
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        val captured = TestContextHolder.testContext().interactions.entrySet().toList()
        captured shouldHaveAtLeastSize 2
        captured.first { it.key.contains("HTTP GET https://api.example.com/users/7") }
            .value.shouldBeInstanceOf<HttpCapturedRequest>()
        captured.first { it.key.contains("HTTP 200") }
            .value.shouldBeInstanceOf<HttpCapturedResponse>()
    }
}
