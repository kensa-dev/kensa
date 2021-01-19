package dev.kensa.util

import dev.kensa.render.diagram.directive.ArrowStyle.UmlSynchronous
import dev.kensa.render.diagram.directive.ArrowStyle.UmlSynchronousDelete
import dev.kensa.util.Attributes.Key.Arrow
import dev.kensa.util.Attributes.Key.Group
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AttributesTest {

    @Test
    internal fun `can get group attribute`() {
        assertThat(Attributes.of(Group, "Test").group).isEqualTo("Test")
        assertThat(Attributes.emptyAttributes().group).isNull()
    }

    @Test
    internal fun `can get line type attribute`() {
        assertThat(Attributes.of(Arrow, UmlSynchronousDelete).arrowStyle).isEqualTo(UmlSynchronousDelete)
        assertThat(Attributes.emptyAttributes().arrowStyle).isEqualTo(UmlSynchronous)
    }

    @Test
    internal fun `can merge attributes`() {
        val a1 = Attributes.of("key1", "val1", "key2", "val2")
        val a2 = Attributes.of("key3", "val3", "key4", "val4")

        val result = a1.merge(a2)

        assertThat(result.toList().map { Pair(it.key, it.value) }).containsExactly(
                Pair("key1", "val1"),
                Pair("key2", "val2"),
                Pair("key3", "val3"),
                Pair("key4", "val4")
        )
    }
}