package dev.kensa.example.hints

import dev.kensa.RenderedHintStrategy.HintFromMethod
import dev.kensa.RenderedValueStrategy.UseToString
import dev.kensa.RenderedValueWithHint
import dev.kensa.example.KotlinExampleTest
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = ToStringField::class, valueStrategy = UseToString, hintStrategy = HintFromMethod, hintParam = "getMetadata")
class KotlinWithMixedStrategyTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        transactionId of "TXN-99"
    }

    val transactionId = ToStringField("ID", "99")
}

class ToStringField(val prefix: String, val suffix: String) {
    fun getMetadata() = "SystemPrefix: $prefix"
    override fun toString() = "$prefix-$suffix"
    infix fun of(value: String) = this
}