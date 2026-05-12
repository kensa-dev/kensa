package dev.kensa.spring.web

import dev.kensa.spring.web.KensaWebAutoConfiguration.Companion.CLIENT_HTTP_INTERCEPTOR_BEAN
import dev.kensa.spring.web.KensaWebAutoConfiguration.Companion.EXCHANGE_FILTER_FUNCTION_BEAN
import dev.kensa.spring.web.KensaWebAutoConfiguration.Companion.HANDLER_INTERCEPTOR_BEAN
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import reactor.core.publisher.Mono

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

    @Test
    fun `user-provided handler interceptor by bean name replaces the default`() {
        contextRunner
            .withUserConfiguration(CustomHandlerInterceptorConfig::class.java)
            .run { ctx ->
                val interceptor = ctx.getBean(HANDLER_INTERCEPTOR_BEAN, HandlerInterceptor::class.java)
                assertThat(interceptor).isInstanceOf(CustomHandlerInterceptor::class.java)
                assertThat(ctx).doesNotHaveBean(KensaHandlerInterceptor::class.java)
            }
    }

    @Test
    fun `user-provided client http interceptor by bean name replaces the default`() {
        contextRunner
            .withUserConfiguration(CustomClientHttpInterceptorConfig::class.java)
            .run { ctx ->
                val interceptor = ctx.getBean(CLIENT_HTTP_INTERCEPTOR_BEAN, ClientHttpRequestInterceptor::class.java)
                assertThat(interceptor).isInstanceOf(CustomClientHttpInterceptor::class.java)
                assertThat(ctx).doesNotHaveBean(KensaClientHttpRequestInterceptor::class.java)
            }
    }

    @Test
    fun `user-provided exchange filter by bean name replaces the default`() {
        contextRunner
            .withUserConfiguration(CustomExchangeFilterConfig::class.java)
            .run { ctx ->
                val filter = ctx.getBean(EXCHANGE_FILTER_FUNCTION_BEAN, ExchangeFilterFunction::class.java)
                assertThat(filter).isInstanceOf(CustomExchangeFilter::class.java)
                assertThat(ctx).doesNotHaveBean(KensaWebClientFilter::class.java)
            }
    }

    @Configuration
    open class CustomHandlerInterceptorConfig {
        @Bean(HANDLER_INTERCEPTOR_BEAN)
        open fun custom(): HandlerInterceptor = CustomHandlerInterceptor()
    }

    @Configuration
    open class CustomClientHttpInterceptorConfig {
        @Bean(CLIENT_HTTP_INTERCEPTOR_BEAN)
        open fun custom(): ClientHttpRequestInterceptor = CustomClientHttpInterceptor()
    }

    @Configuration
    open class CustomExchangeFilterConfig {
        @Bean(EXCHANGE_FILTER_FUNCTION_BEAN)
        open fun custom(): ExchangeFilterFunction = CustomExchangeFilter()
    }

    class CustomHandlerInterceptor : HandlerInterceptor

    class CustomClientHttpInterceptor : ClientHttpRequestInterceptor {
        override fun intercept(
            request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution,
        ): ClientHttpResponse = execution.execute(request, body)
    }

    class CustomExchangeFilter : ExchangeFilterFunction {
        override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<org.springframework.web.reactive.function.client.ClientResponse> =
            next.exchange(request)
    }
}
