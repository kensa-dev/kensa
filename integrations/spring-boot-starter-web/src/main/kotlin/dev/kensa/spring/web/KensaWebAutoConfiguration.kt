package dev.kensa.spring.web

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Each capture point is exposed as a named bean (constants below). To plug in a
 * party-aware interceptor for the sequence diagram, register a bean with the same name
 * and a broader interface type — the defaults are conditional on the *name* being
 * missing rather than the concrete class, so a `HandlerInterceptor` subclass overrides
 * cleanly without having to extend [KensaHandlerInterceptor].
 */
@AutoConfiguration
class KensaWebAutoConfiguration {

    companion object {
        const val HANDLER_INTERCEPTOR_BEAN = "kensaHandlerInterceptor"
        const val WEB_MVC_CONFIGURER_BEAN = "kensaWebMvcConfigurer"
        const val CLIENT_HTTP_INTERCEPTOR_BEAN = "kensaClientHttpRequestInterceptor"
        const val REST_TEMPLATE_CUSTOMIZER_BEAN = "kensaRestTemplateCustomizer"
        const val EXCHANGE_FILTER_FUNCTION_BEAN = "kensaExchangeFilterFunction"
        const val WEB_CLIENT_CUSTOMIZER_BEAN = "kensaWebClientCustomizer"
    }

    @AutoConfiguration
    @ConditionalOnClass(HandlerInterceptor::class)
    open class WebMvc {

        @Bean(HANDLER_INTERCEPTOR_BEAN)
        @ConditionalOnMissingBean(name = [HANDLER_INTERCEPTOR_BEAN])
        open fun kensaHandlerInterceptor(): HandlerInterceptor = KensaHandlerInterceptor()

        @Bean(WEB_MVC_CONFIGURER_BEAN)
        @ConditionalOnMissingBean(name = [WEB_MVC_CONFIGURER_BEAN])
        open fun kensaWebMvcConfigurer(
            @Qualifier(HANDLER_INTERCEPTOR_BEAN) interceptor: HandlerInterceptor,
        ): WebMvcConfigurer =
            object : WebMvcConfigurer {
                override fun addInterceptors(registry: InterceptorRegistry) {
                    registry.addInterceptor(interceptor)
                }
            }
    }

    @AutoConfiguration
    @ConditionalOnClass(RestTemplate::class)
    open class RestClient {

        @Bean(CLIENT_HTTP_INTERCEPTOR_BEAN)
        @ConditionalOnMissingBean(name = [CLIENT_HTTP_INTERCEPTOR_BEAN])
        open fun kensaClientHttpRequestInterceptor(): ClientHttpRequestInterceptor =
            KensaClientHttpRequestInterceptor()

        @Bean(REST_TEMPLATE_CUSTOMIZER_BEAN)
        @ConditionalOnMissingBean(name = [REST_TEMPLATE_CUSTOMIZER_BEAN])
        open fun kensaRestTemplateCustomizer(
            @Qualifier(CLIENT_HTTP_INTERCEPTOR_BEAN) interceptor: ClientHttpRequestInterceptor,
        ): RestTemplateCustomizer =
            RestTemplateCustomizer { it.interceptors.add(interceptor) }
    }

    @AutoConfiguration
    @ConditionalOnClass(WebClient::class)
    open class Reactive {

        @Bean(EXCHANGE_FILTER_FUNCTION_BEAN)
        @ConditionalOnMissingBean(name = [EXCHANGE_FILTER_FUNCTION_BEAN])
        open fun kensaExchangeFilterFunction(): ExchangeFilterFunction = KensaWebClientFilter()

        @Bean(WEB_CLIENT_CUSTOMIZER_BEAN)
        @ConditionalOnMissingBean(name = [WEB_CLIENT_CUSTOMIZER_BEAN])
        open fun kensaWebClientCustomizer(
            @Qualifier(EXCHANGE_FILTER_FUNCTION_BEAN) filter: ExchangeFilterFunction,
        ): WebClientCustomizer =
            WebClientCustomizer { it.filter(filter) }
    }
}
