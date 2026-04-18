package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

class KotlinExpandableSentenceSource {

    @ExpandableSentence
    fun expandableFromSource(@RenderedValue value: String?) {
        assertThat(value).isEqualTo("foo")
    }
}
