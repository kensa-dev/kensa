package dev.kensa.kotest.testsupport.field

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.be
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test

class MatcherFieldTest {

    @Test
    fun `description splits camelCase name into spaced words`() {
        anyField("aProviderCode").description shouldBe "Provider Code"
    }

    @Test
    fun `description drops leading articles regardless of case`() {
        anyField("aFoo").description shouldBe "Foo"
        anyField("anApple").description shouldBe "Apple"
        anyField("TheBigThing").description shouldBe "Big Thing"
    }

    @Test
    fun `description drops trailing Field suffix`() {
        anyField("aProviderField").description shouldBe "Provider"
        anyField("anIdField").description shouldBe "Id"
    }

    @Test
    fun `description splits camelCase even without articles or Field suffix`() {
        anyField("ProviderCode").description shouldBe "Provider Code"
    }

    @Test
    fun `of passes when extracted equals expected`() {
        val field = stringField("aProviderCode") { "X" }
        (field of "X").test("anything").passed() shouldBe true
    }

    @Test
    fun `of fails with description-prefixed message when extracted differs`() {
        val field = stringField("aProviderCode") { "X" }
        val result = (field of "Y").test("anything")
        result.passed() shouldBe false
        result.failureMessage() shouldStartWith "Provider Code:"
    }

    @Test
    fun `of passes when both extracted and expected are null`() {
        val field = stringField("aFoo") { null }
        (field of null).test("subject").passed() shouldBe true
    }

    @Test
    fun `of with null subject extracts null and compares to expected`() {
        val field = stringField("aFoo") { error("should not be invoked") }
        (field of null).test(null).passed() shouldBe true
        (field of "X").test(null).passed() shouldBe false
    }

    @Test
    fun `matching with single matcher delegates to that matcher`() {
        val field = stringField("aFoo") { "abc" }
        (field matching be("abc")).test("subject").passed() shouldBe true
        (field matching be("xyz")).test("subject").passed() shouldBe false
    }

    @Test
    fun `matching with multiple matchers passes only when all pass`() {
        val field = stringField("aFoo") { "abc" }
        field.matching(be("abc"), startsWithA(), endsWithC()).test("subject").passed() shouldBe true
        field.matching(be("abc"), be("zzz")).test("subject").passed() shouldBe false
    }

    @Test
    fun `matching with regex passes when extracted String matches`() {
        val field = stringField("aFoo") { "abc-123" }
        (field matching "[a-z]+-\\d+").test("subject").passed() shouldBe true
        (field matching "\\d+").test("subject").passed() shouldBe false
    }

    @Test
    fun `matching prefixes failure message with description`() {
        val field = stringField("aProviderCode") { "abc" }
        val result = (field matching be("zzz")).test("subject")
        result.passed() shouldBe false
        result.failureMessage() shouldStartWith "Provider Code:"
    }

    @Test
    fun `withListOf passes for exact-order list match`() {
        val field = stringListField("items") { listOf("a", "b", "c") }
        field.withListOf("a", "b", "c").test("subject").passed() shouldBe true
        field.withListOf("c", "b", "a").test("subject").passed() shouldBe false
    }

    @Test
    fun `withListOf fails when expected has different elements`() {
        val field = stringListField("items") { listOf("a", "b") }
        field.withListOf("a", "b", "c").test("subject").passed() shouldBe false
    }

    @Test
    fun `withSetOf passes for any-order set match`() {
        val field = stringSetField("items") { setOf("a", "b", "c") }
        field.withSetOf("c", "a", "b").test("subject").passed() shouldBe true
    }

    @Test
    fun `withSetOf fails when sizes differ`() {
        val field = stringSetField("items") { setOf("a", "b") }
        field.withSetOf("a", "b", "c").test("subject").passed() shouldBe false
    }

    @Test
    fun `toMatcher applies converter before equality check`() {
        val field = anyField("aWrapper", extract = { _: String -> Wrapper("payload") })
        field.toMatcher("payload") { it.text }.test("subject").passed() shouldBe true
        field.toMatcher("other") { it.text }.test("subject").passed() shouldBe false
    }

    @Test
    fun `toMatcher wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field of "x").test("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with matcher wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field matching be("x")).test("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with regex wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field matching ".*").test("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with vararg wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { field.matching(be("x"), startsWithA()).test("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    private fun Throwable?.shouldNotBeNullAndMessageContains(fragment: String) {
        require(this != null) { "Expected exception but none thrown" }
        require(message?.contains(fragment) == true) { "Expected message to contain '$fragment' but was: $message" }
    }

    private fun startsWithA(): Matcher<String?> = Matcher { value ->
        MatcherResult(value?.startsWith("a") == true, { "expected start with a" }, { "expected not to start with a" })
    }

    private fun endsWithC(): Matcher<String?> = Matcher { value ->
        MatcherResult(value?.endsWith("c") == true, { "expected end with c" }, { "expected not to end with c" })
    }

    private data class Wrapper(val text: String)
}

private fun anyField(theName: String): MatcherField<Any, Any> = object : MatcherField<Any, Any> {
    override val name = theName
    override fun extract(value: Any): Any? = null
}

private fun <R> anyField(theName: String, extract: (String) -> R?): MatcherField<String, R> = object : MatcherField<String, R> {
    override val name = theName
    override fun extract(value: String): R? = extract(value)
}

private fun stringField(theName: String, extract: (String) -> String?): MatcherField<String, String> = object : MatcherField<String, String> {
    override val name = theName
    override fun extract(value: String): String? = extract(value)
}

private fun stringListField(theName: String, extract: (String) -> List<String>?): MatcherField<String, List<String>> = object : MatcherField<String, List<String>> {
    override val name = theName
    override fun extract(value: String): List<String>? = extract(value)
}

private fun stringSetField(theName: String, extract: (String) -> Set<String>?): MatcherField<String, Set<String>> = object : MatcherField<String, Set<String>> {
    override val name = theName
    override fun extract(value: String): Set<String>? = extract(value)
}
