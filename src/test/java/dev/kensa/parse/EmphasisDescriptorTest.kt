package dev.kensa.parse

import dev.kensa.Colour
import dev.kensa.Colour.BackgroundLight
import dev.kensa.Colour.TextBlack
import dev.kensa.TextStyle.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class EmphasisDescriptorTest {
    @Test
    internal fun `can extract emphasis as css`() {
        assertThat(EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), TextBlack, BackgroundLight).asCss()).containsExactly(
                "has-background-light",
                "has-text-black",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }

    @Test
    internal fun `generates empty css for default emphasis descriptor`() {
        assertThat(EmphasisDescriptor.Default.asCss()).isEmpty()
    }

    @Test
    internal fun `css generation ignores default text colour`() {
        assertThat(EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), Colour.Default, BackgroundLight).asCss()).containsExactly(
                "has-background-light",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }

    @Test
    internal fun `css generation ignores default background colour`() {
        assertThat(EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), TextBlack, Colour.Default).asCss()).containsExactly(
                "has-text-black",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }
}