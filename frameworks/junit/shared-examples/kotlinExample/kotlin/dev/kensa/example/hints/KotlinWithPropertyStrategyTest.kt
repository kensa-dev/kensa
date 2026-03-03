package dev.kensa.example.hints

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseProperty
import dev.kensa.RenderedValueWithHint
import dev.kensa.example.KotlinExampleTest
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = DataField::class, valueStrategy = UseProperty, valueParam = "label", hintStrategy = HintFromProperty, hintParam = "xpath")
class KotlinWithPropertyStrategyTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        accountBalance of "£100.00"
    }

    val accountBalance = DataField("Account Balance", "//div[@id='balance']")
}

data class DataField(val label: String, val xpath: String) {
    infix fun of(value: String) = this
}