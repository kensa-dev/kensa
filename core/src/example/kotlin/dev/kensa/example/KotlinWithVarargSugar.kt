package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.RefinedSugar.containing
import dev.kensa.RefinedSugar.has
import dev.kensa.RefinedSugar.have
import dev.kensa.RefinedSugar.having
import dev.kensa.RefinedSugar.including
import dev.kensa.RefinedSugar.with
import dev.kensa.RenderedValue
import dev.kensa.StateCollector

@Suppress("unused")
class KotlinWithVarargSugar {

    @RenderedValue
    private val aStatus = 200

    @RenderedValue
    private val aBody = "ok"

    fun havingTest() {
        assertThat(theResponse(*having(aStatus, aBody))).isEqualTo("ok")
    }

    fun otherWordsTest() {
        assertThat(theResponse(*has(aStatus, aBody))).isEqualTo("ok")
        assertThat(theResponse(*have(aStatus, aBody))).isEqualTo("ok")
        assertThat(theResponse(*including(aStatus, aBody))).isEqualTo("ok")
        assertThat(theResponse(*containing(aStatus, aBody))).isEqualTo("ok")
        assertThat(theResponse(*with(aStatus, aBody))).isEqualTo("ok")
    }

    fun singleArgumentTest() {
        val status: Int = has(aStatus)
        val body: String = with(aBody)
        val scoped: Int = with(aBody) { length }
    }

    private fun theResponse(vararg items: Any) = StateCollector { "ok" }
}
