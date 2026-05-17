package dev.kensa.junit

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

internal class JUnitTagsForTest {

    @Tag("smoke")
    @Tag("regression")
    class TaggedClass {
        @Tag("slow")
        fun taggedMethod() {
        }

        fun untaggedMethod() {
        }
    }

    class UntaggedClass {
        fun method() {
        }
    }

    @Test
    fun returnsAllTagsOnClass() {
        junit6Descriptor.tagsFor(TaggedClass::class.java).shouldContainExactly("smoke", "regression")
    }

    @Test
    fun returnsMethodLevelTags() {
        val method = TaggedClass::class.java.getDeclaredMethod("taggedMethod")
        junit6Descriptor.tagsFor(method).shouldContainExactly("slow")
    }

    @Test
    fun returnsEmptyListWhenNoTagsOnClass() {
        junit6Descriptor.tagsFor(UntaggedClass::class.java) shouldBe emptyList()
    }

    @Test
    fun returnsEmptyListWhenNoTagsOnMethod() {
        val method = TaggedClass::class.java.getDeclaredMethod("untaggedMethod")
        junit6Descriptor.tagsFor(method) shouldBe emptyList()
    }
}
