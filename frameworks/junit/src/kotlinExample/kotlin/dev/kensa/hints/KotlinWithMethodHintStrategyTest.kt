package dev.kensa.hints

import dev.kensa.RenderedHintStrategy
import dev.kensa.RenderedValueStrategy.UseMethod
import dev.kensa.RenderedValueWithHint
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = MethodField::class, valueStrategy = UseMethod, valueParam = "displayName", hintStrategy = RenderedHintStrategy.HintFromMethod, hintParam = "technicalPath")
class KotlinWithMethodHintStrategyTest : KensaTest {

    @Test
    fun passingTest() {
        userEmail of "test@example.com"
    }

    val userEmail = MethodField("User Email", "/api/v1/user/email")
}

class MethodField(private val name: String, private val path: String) {
    fun displayName() = name
    fun technicalPath() = path
    infix fun of(value: String) = this
}
