package dev.kensa.kotest

import io.kotest.core.annotation.Tags
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KotestDescriptorTagsTest {

    @Tags("api", "smoke")
    private class TaggedSpec

    private class UntaggedSpec

    @Tags("api", "smoke", "api")
    private class DuplicateTagsSpec

    @Test
    fun `tagsFor returns names from a Tags annotation in declared order`() {
        kotestDescriptor.tagsFor(TaggedSpec::class.java) shouldContainExactly listOf("api", "smoke")
    }

    @Test
    fun `tagsFor returns empty list when no Tags annotation is present`() {
        kotestDescriptor.tagsFor(UntaggedSpec::class.java) shouldBe emptyList()
    }

    @Test
    fun `tagsFor deduplicates tag names preserving first occurrence order`() {
        kotestDescriptor.tagsFor(DuplicateTagsSpec::class.java) shouldContainExactly listOf("api", "smoke")
    }
}
