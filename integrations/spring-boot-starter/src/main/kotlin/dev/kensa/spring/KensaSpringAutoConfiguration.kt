package dev.kensa.spring

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(KensaSpringProperties::class)
class KensaSpringAutoConfiguration
