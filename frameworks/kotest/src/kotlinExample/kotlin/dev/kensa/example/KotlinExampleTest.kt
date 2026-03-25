package dev.kensa.example

import dev.kensa.Kensa.konfigure
import dev.kensa.kotest.KensaTest
import kotlin.io.path.Path

abstract class KotlinExampleTest : KensaTest() {
    @BeforeEach
    fun setUpSourceLocations() {
        konfigure {
            sourceLocations = listOf(
                Path("${System.getProperty("user.dir")}/src/kotlinExample/kotlin")
            )
        }
    }

}

