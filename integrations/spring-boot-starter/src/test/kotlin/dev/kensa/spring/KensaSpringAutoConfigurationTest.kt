package dev.kensa.spring

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class KensaSpringAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KensaSpringAutoConfiguration::class.java))

    @Test
    fun `registers KensaSpringProperties bean`() {
        contextRunner.run { ctx ->
            assertThat(ctx).hasSingleBean(KensaSpringProperties::class.java)
        }
    }

    @Test
    fun `binds kensa properties from environment`() {
        contextRunner
            .withPropertyValues("kensa.title-text=Wired", "kensa.tab-size=7")
            .run { ctx ->
                val props = ctx.getBean(KensaSpringProperties::class.java)
                props.titleText shouldBe "Wired"
                props.tabSize shouldBe 7
            }
    }
}
