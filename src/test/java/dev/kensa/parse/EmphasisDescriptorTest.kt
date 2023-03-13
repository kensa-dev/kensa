package dev.kensa.parse

import dev.kensa.Colour
import dev.kensa.Colour.BackgroundLight
import dev.kensa.Colour.TextBlack
import dev.kensa.TextStyle.*
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

internal class EmphasisDescriptorTest {
    @Test
    internal fun `can extract emphasis as css`() {
        EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), TextBlack, BackgroundLight).asCss().shouldContainExactly(
                "has-background-light",
                "has-text-black",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }

    @Test
    internal fun `generates empty css for default emphasis descriptor`() {
        EmphasisDescriptor.Default.asCss().shouldBeEmpty()
    }

    @Test
    internal fun `css generation ignores default text colour`() {
        EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), Colour.Default, BackgroundLight).asCss().shouldContainExactly(
                "has-background-light",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }

    @Test
    internal fun `css generation ignores default background colour`() {
        EmphasisDescriptor(setOf(Capitalized, Italic, Lowercase), TextBlack, Colour.Default).asCss().shouldContainExactly(
                "has-text-black",
                "is-capitalized",
                "is-italic",
                "is-lowercase"
        )
    }
}