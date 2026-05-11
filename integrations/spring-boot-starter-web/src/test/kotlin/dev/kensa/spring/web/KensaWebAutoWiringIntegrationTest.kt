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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@KensaTest
@AutoConfigureMockMvc
@Execution(SAME_THREAD)
class KensaWebAutoWiringIntegrationTest {

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    open class TestApp {

        @RestController
        class Echo {
            @GetMapping("/echo")
            fun echo(): Map<String, String> = mapOf("ok" to "true")
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `auto-configured handler interceptor captures interactions without manual wiring`() {
        mockMvc.perform(get("/echo")).andExpect(status().isOk)

        val captured = TestContextHolder.testContext().interactions.entrySet().toList()
        captured shouldHaveAtLeastSize 2
        captured.first { it.key.contains("HTTP GET /echo") }
            .value.shouldBeInstanceOf<HttpCapturedRequest>()
        captured.first { it.key.contains("HTTP 200") }
            .value.shouldBeInstanceOf<HttpCapturedResponse>()
    }
}
