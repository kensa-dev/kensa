package dev.kensa.example

import dev.kensa.Kensa.konfigure
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.BeforeEach
import kotlin.io.path.Path

abstract class KotlinExampleTest : KensaTest {
    @BeforeEach
    fun setUpSourceLocations() {
        konfigure {
            sourceLocations = listOf(
                Path("${System.getProperty("user.dir")}/src/kotlinExample/kotlin")
            )
        }
    }

}