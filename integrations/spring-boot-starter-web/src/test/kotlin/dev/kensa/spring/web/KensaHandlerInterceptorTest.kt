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
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@KensaTest
@AutoConfigureMockMvc
@Execution(SAME_THREAD)
class KensaHandlerInterceptorTest {

    @SpringBootConfiguration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    open class TestApp {

        @RestController
        class Greeter {
            @GetMapping("/hello")
            fun hello(): String = "hi"
        }

        @Bean
        open fun kensaInterceptor(): KensaHandlerInterceptor = KensaHandlerInterceptor()

        @Bean
        open fun kensaWebMvcConfigurer(interceptor: KensaHandlerInterceptor): WebMvcConfigurer =
            object : WebMvcConfigurer {
                override fun addInterceptors(registry: InterceptorRegistry) {
                    registry.addInterceptor(interceptor)
                }
            }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `captures request and response interactions when the handler runs inside a Kensa test`() {
        mockMvc.perform(get("/hello")).andExpect(status().isOk)

        val interactions = TestContextHolder.testContext().interactions
        val captured = interactions.entrySet().toList()

        captured shouldHaveAtLeastSize 2
        captured.first { it.key.contains("HTTP GET /hello") }.value.shouldBeInstanceOf<HttpCapturedRequest>()
        captured.first { it.key.contains("HTTP 200") }.value.shouldBeInstanceOf<HttpCapturedResponse>()
    }
}
