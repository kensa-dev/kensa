package dev.kensa.attachments

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttachmentsTest {

    private val stringKey = TypedKey<String>("greeting")
    private val intKey = TypedKey<Int>("count")

    @Test
    fun `get returns null when key absent`() {
        Attachments().getOrNull(stringKey).shouldBeNull()
    }

    @Test
    fun `put then get returns the stored value with correct type`() {
        val attachments = Attachments()
        attachments.put(stringKey, "hello")
        attachments.getOrNull(stringKey) shouldBe "hello"
    }

    @Test
    fun `put with two distinct keys keeps values independent`() {
        val attachments = Attachments()
        attachments.put(stringKey, "hello")
        attachments.put(intKey, 42)
        attachments.getOrNull(stringKey) shouldBe "hello"
        attachments.getOrNull(intKey) shouldBe 42
    }

    @Test
    fun `put with same key overwrites previous value`() {
        val attachments = Attachments()
        attachments.put(stringKey, "first")
        attachments.put(stringKey, "second")
        attachments.getOrNull(stringKey) shouldBe "second"
    }
}
