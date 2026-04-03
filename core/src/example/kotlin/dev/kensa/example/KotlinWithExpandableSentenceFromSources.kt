package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.Sources

@Sources(KotlinExpandableSentenceSource::class)
class KotlinWithExpandableSentenceFromSources {

    fun testWithExpandableSentenceFromSource() {
        assertThat("foo").isEqualTo("foo")
    }
}
