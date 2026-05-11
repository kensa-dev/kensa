package dev.kensa.spring.web

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@AutoConfiguration
class KensaWebAutoConfiguration {

    @AutoConfiguration
    @ConditionalOnClass(HandlerInterceptor::class)
    open class WebMvc {

        @Bean
        @ConditionalOnMissingBean
        open fun kensaHandlerInterceptor(): KensaHandlerInterceptor = KensaHandlerInterceptor()

        @Bean("kensaWebMvcConfigurer")
        @ConditionalOnMissingBean(name = ["kensaWebMvcConfigurer"])
        open fun kensaWebMvcConfigurer(interceptor: KensaHandlerInterceptor): WebMvcConfigurer =
            object : WebMvcConfigurer {
                override fun addInterceptors(registry: InterceptorRegistry) {
                    registry.addInterceptor(interceptor)
                }
            }
    }

    @AutoConfiguration
    @ConditionalOnClass(RestTemplate::class)
    open class RestClient {

        @Bean
        @ConditionalOnMissingBean
        open fun kensaRestTemplateCustomizer(): RestTemplateCustomizer =
            RestTemplateCustomizer { it.interceptors.add(KensaClientHttpRequestInterceptor()) }
    }

    @AutoConfiguration
    @ConditionalOnClass(WebClient::class)
    open class Reactive {

        @Bean
        @ConditionalOnMissingBean
        open fun kensaWebClientCustomizer(): WebClientCustomizer =
            WebClientCustomizer { it.filter(KensaWebClientFilter()) }
    }
}
