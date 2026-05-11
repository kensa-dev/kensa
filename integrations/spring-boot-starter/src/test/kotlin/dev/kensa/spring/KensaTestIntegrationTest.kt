package dev.kensa.spring

import dev.kensa.Kensa
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration

@KensaTest
@Execution(SAME_THREAD)
class KensaTestIntegrationTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    class TestApp

    @Test
    fun `kensa configuration reflects values bound from application yaml`() {
        Kensa.konfigure {
            titleText shouldBe "From Spring YAML"
            tabSize shouldBe 5
            setupStrategy shouldBe SetupStrategy.Grouped
            isOutputEnabled shouldBe false
        }
    }
}
