package dev.kensa.parse

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

internal class HighlightDescriptorTest {
    @Test
    internal fun `extracts correct css classes`() {
        HighlightDescriptor("some value", "some name", "some-index").asCss().shouldContainExactly(
                "tk-hl",
                "tk-hl-some-index",
        )
    }
}