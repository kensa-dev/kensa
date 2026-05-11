package dev.kensa.spring.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

class KensaWebAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                KensaWebAutoConfiguration::class.java,
                KensaWebAutoConfiguration.WebMvc::class.java,
                KensaWebAutoConfiguration.RestClient::class.java,
                KensaWebAutoConfiguration.Reactive::class.java,
            )
        )

    @Test
    fun `registers all interceptors when web mvc, rest template, and web client are on the classpath`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(KensaHandlerInterceptor::class.java)
            assertThat(ctx).hasSingleBean(WebMvcConfigurer::class.java)
            assertThat(ctx).hasSingleBean(RestTemplateCustomizer::class.java)
            assertThat(ctx).hasSingleBean(WebClientCustomizer::class.java)
        }
    }

    @Test
    fun `omits the handler interceptor when spring-webmvc is absent`() {
        contextRunner
            .withClassLoader(FilteredClassLoader(HandlerInterceptor::class.java))
            .run { ctx ->
                assertThat(ctx).doesNotHaveBean(KensaHandlerInterceptor::class.java)
            }
    }

    @Test
    fun `omits the rest template customizer when RestTemplate is absent`() {
        contextRunner
            .withClassLoader(FilteredClassLoader(RestTemplate::class.java))
            .run { ctx ->
                assertThat(ctx).doesNotHaveBean(RestTemplateCustomizer::class.java)
            }
    }

    @Test
    fun `omits the web client customizer when WebClient is absent`() {
        contextRunner
            .withClassLoader(FilteredClassLoader(WebClient::class.java))
            .run { ctx ->
                assertThat(ctx).doesNotHaveBean(WebClientCustomizer::class.java)
            }
    }
}
