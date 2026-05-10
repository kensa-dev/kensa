package dev.kensa.hamkrest.testsupport.field

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.equalTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
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
        (field of "X").invoke("anything") shouldBe MatchResult.Match
    }

    @Test
    fun `of fails with description-prefixed matcher description when extracted differs`() {
        val field = stringField("aProviderCode") { "X" }
        val matcher = field of "Y"
        matcher.invoke("anything").shouldBeInstanceOf<MatchResult.Mismatch>()
        matcher.description shouldStartWith "Provider Code:"
    }

    @Test
    fun `of passes when both extracted and expected are null`() {
        val field = stringField("aFoo") { null }
        (field of null).invoke("subject") shouldBe MatchResult.Match
    }

    @Test
    fun `of with null subject extracts null and compares to expected`() {
        val field = stringField("aFoo") { error("should not be invoked") }
        (field of null).invoke(null) shouldBe MatchResult.Match
        (field of "X").invoke(null).shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `matching with single matcher delegates to that matcher`() {
        val field = stringField("aFoo") { "abc" }
        (field matching equalTo<String?>("abc")).invoke("subject") shouldBe MatchResult.Match
        (field matching equalTo<String?>("xyz")).invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `matching with multiple matchers passes only when all pass`() {
        val field = stringField("aFoo") { "abc" }
        field.matching(equalTo<String?>("abc"), startsWithA(), endsWithC()).invoke("subject") shouldBe MatchResult.Match
        field.matching(equalTo<String?>("abc"), equalTo<String?>("zzz")).invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `matching with regex passes when extracted String matches`() {
        val field = stringField("aFoo") { "abc-123" }
        (field matching "[a-z]+-\\d+").invoke("subject") shouldBe MatchResult.Match
        (field matching "\\d+").invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `matching prefixes description with field description`() {
        val field = stringField("aProviderCode") { "abc" }
        val matcher = field matching equalTo<String?>("zzz")
        matcher.description shouldStartWith "Provider Code:"
    }

    @Test
    fun `withListOf passes for exact-order list match`() {
        val field = stringListField("items") { listOf("a", "b", "c") }
        field.withListOf("a", "b", "c").invoke("subject") shouldBe MatchResult.Match
        field.withListOf("c", "b", "a").invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `withListOf fails when expected has different elements`() {
        val field = stringListField("items") { listOf("a", "b") }
        field.withListOf("a", "b", "c").invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `withSetOf passes for any-order set match`() {
        val field = stringSetField("items") { setOf("a", "b", "c") }
        field.withSetOf("c", "a", "b").invoke("subject") shouldBe MatchResult.Match
    }

    @Test
    fun `withSetOf fails when sizes differ`() {
        val field = stringSetField("items") { setOf("a", "b") }
        field.withSetOf("a", "b", "c").invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `toMatcher applies converter before equality check`() {
        val field = anyField("aWrapper", extract = { _: String -> Wrapper("payload") })
        field.toMatcher("payload") { it.text }.invoke("subject") shouldBe MatchResult.Match
        field.toMatcher("other") { it.text }.invoke("subject").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `of wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field of "x").invoke("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with matcher wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field matching equalTo<String?>("x")).invoke("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with regex wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { (field matching ".*").invoke("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    @Test
    fun `matching with vararg wraps extraction exception with description`() {
        val field = stringField("aFoo") { error("boom") }
        val ex = runCatching { field.matching(equalTo<String?>("x"), startsWithA()).invoke("subject") }.exceptionOrNull()
        ex.shouldNotBeNullAndMessageContains("Foo")
    }

    private fun Throwable?.shouldNotBeNullAndMessageContains(fragment: String) {
        require(this != null) { "Expected exception but none thrown" }
        message?.shouldContain(fragment) ?: error("Message was null; expected to contain '$fragment'")
    }

    private fun startsWithA(): Matcher<String?> = object : Matcher<String?> {
        override val description = "starts with a"
        override fun invoke(actual: String?) = if (actual?.startsWith("a") == true) MatchResult.Match else MatchResult.Mismatch("did not start with a")
    }

    private fun endsWithC(): Matcher<String?> = object : Matcher<String?> {
        override val description = "ends with c"
        override fun invoke(actual: String?) = if (actual?.endsWith("c") == true) MatchResult.Match else MatchResult.Mismatch("did not end with c")
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
