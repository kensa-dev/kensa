package dev.kensa

import com.natpryce.hamkrest.equalTo
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithNotesTest : KotlinExampleTest(), WithHamkrest {

    @Test
    fun passingTest() {
        /// These are notes
        given(something())

        /// These are also notes
        /// on two lines
        whenever(somethingHappens())

        /// Notes on then
        then(
            things(), /// End of line notes
            equalTo(true) /// More end of line notes
        )
    }

    fun something() = Action<GivensContext> {}
    fun somethingHappens() = Action<ActionContext> {}
    fun things() = StateCollector { true }
}