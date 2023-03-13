package dev.kensa.util

import dev.kensa.render.diagram.directive.ArrowStyle.UmlSynchronous
import dev.kensa.render.diagram.directive.ArrowStyle.UmlSynchronousDelete
import dev.kensa.util.Attributes.Key.Arrow
import dev.kensa.util.Attributes.Key.Group
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class AttributesTest {

    @Test
    internal fun `can get group attribute`() {
        Attributes.of(Group, "Test").group shouldBe "Test"
        Attributes.emptyAttributes().group.shouldBeNull()
    }

    @Test
    internal fun `can get line type attribute`() {
        Attributes.of(Arrow, UmlSynchronousDelete).arrowStyle shouldBe UmlSynchronousDelete
        Attributes.emptyAttributes().arrowStyle shouldBe UmlSynchronous
    }

    @Test
    internal fun `can merge attributes`() {
        val a1 = Attributes.of("key1", "val1", "key2", "val2")
        val a2 = Attributes.of("key3", "val3", "key4", "val4")

        val result = a1.merge(a2)

        result.toList().map { Pair(it.key, it.value) }.shouldContainExactly(
                Pair("key1", "val1"),
                Pair("key2", "val2"),
                Pair("key3", "val3"),
                Pair("key4", "val4")
        )
    }
}