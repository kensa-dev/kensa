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
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

@KensaTest
@Execution(SAME_THREAD)
class KensaClientHttpRequestInterceptorTest {

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    open class TestApp {

        @Bean
        open fun restTemplate(): RestTemplate = RestTemplate().apply {
            interceptors.add(KensaClientHttpRequestInterceptor())
        }
    }

    @Autowired
    lateinit var restTemplate: RestTemplate

    @Test
    fun `captures outbound request and inbound response interactions`() {
        val server = MockRestServiceServer.createServer(restTemplate)
        server.expect(requestTo("https://api.example.com/users/42"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"id":42}""", APPLICATION_JSON))

        restTemplate.getForObject("https://api.example.com/users/42", String::class.java)

        val captured = TestContextHolder.testContext().interactions.entrySet().toList()
        captured shouldHaveAtLeastSize 2
        captured.first { it.key.contains("HTTP GET https://api.example.com/users/42") }
            .value.shouldBeInstanceOf<HttpCapturedRequest>()
        captured.first { it.key.contains("HTTP 200") }
            .value.shouldBeInstanceOf<HttpCapturedResponse>()
    }
}
