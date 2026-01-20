package dev.kensa.parse

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ImportsTest {

    private val declaringClass = ImportsTest::class.java
    private val emptyImports = Imports(emptySet(), emptySet(), declaringClass)

    @Test
    fun `matches when type is explicitly imported`() {
        val imports = Imports(setOf(ArrayList::class.java, Map::class.java), emptySet(), declaringClass)

        imports.match(arrayOf(ArrayList::class.java), listOf("ArrayList")) shouldBe true
        imports.match(arrayOf(Map.Entry::class.java), listOf("Entry")) shouldBe true
        imports.match(arrayOf(Map.Entry::class.java), listOf("Map.Entry")) shouldBe true
    }

    @Test
    fun `matches when type is in wildcard package`() {
        val imports = Imports(emptySet(), setOf("java.util"), declaringClass)

        imports.match(arrayOf(ArrayList::class.java), listOf("ArrayList")) shouldBe true
        imports.match(arrayOf(HashSet::class.java), listOf("HashSet")) shouldBe true
    }

    @Test
    fun `matches nested classes using dots in source`() {
        val methodTypes: Array<Class<*>> = arrayOf(Map.Entry::class.java)
        val sourceTypes = listOf("java.util.Map.Entry")

        emptyImports.match(methodTypes, sourceTypes) shouldBe true
    }

    @Test
    fun `returns false if parameter counts differ`() {
        val methodTypes: Array<Class<*>> = arrayOf(String::class.java)
        val sourceTypes = listOf("String", "Int")

        emptyImports.match(methodTypes, sourceTypes) shouldBe false
    }

    @Test
    fun `matches simple names for default packages`() {
        val methodTypes: Array<Class<*>> = arrayOf(String::class.java, Int::class.javaObjectType)
        val sourceTypes = listOf("String", "Int")

        emptyImports.match(methodTypes, sourceTypes) shouldBe true
    }

    @Test
    fun `matches types with generics in source`() {
        val methodTypes: Array<Class<*>> = arrayOf(List::class.java)
        val sourceTypes = listOf("List<String>")

        emptyImports.match(methodTypes, sourceTypes) shouldBe true
    }

    @Test
    fun `matches fully qualified names in source`() {
        val methodTypes: Array<Class<*>> = arrayOf(ArrayList::class.java)
        val sourceTypes = listOf("java.util.ArrayList")

        emptyImports.match(methodTypes, sourceTypes) shouldBe true
    }

    @Test
    fun `returns false when names do not match even if type is imported`() {
        val imports = Imports(setOf(ArrayList::class.java), emptySet(), declaringClass)
        val methodTypes: Array<Class<*>> = arrayOf(String::class.java)
        val sourceTypes = listOf("ArrayList")

        imports.match(methodTypes, sourceTypes) shouldBe false
    }

    @Test
    fun `matches all equivalent kotlin primitives`() {
        emptyImports.match(arrayOf(Int::class.java), listOf("Int")) shouldBe true
        emptyImports.match(arrayOf(Int::class.javaObjectType), listOf("Int")) shouldBe true
        emptyImports.match(arrayOf(Long::class.java), listOf("Long")) shouldBe true
        emptyImports.match(arrayOf(Boolean::class.java), listOf("Boolean")) shouldBe true
        emptyImports.match(arrayOf(Double::class.java), listOf("Double")) shouldBe true
    }

    @Test
    fun `matches types in same package`() {
        val methodTypes: Array<Class<*>> = arrayOf(MethodDeclarations::class.java)
        val sourceTypes = listOf("MethodDeclarations")

        emptyImports.match(methodTypes, sourceTypes) shouldBe true
    }

    @Test
    fun `matches types in default packages`() {
        emptyImports.match(arrayOf(String::class.java), listOf("String")) shouldBe true // java.lang
        emptyImports.match(arrayOf(Regex::class.java), listOf("Regex")) shouldBe true   // kotlin.text
    }

    @Test
    fun `returns false when package does not match for non-imported class`() {
        val methodTypes: Array<Class<*>> = arrayOf(java.sql.Date::class.java)
        val sourceTypes = listOf("Date")

        emptyImports.match(methodTypes, sourceTypes) shouldBe false
    }

    @Test
    fun `returns false if types are zipped incorrectly`() {
        val methodTypes: Array<Class<*>> = arrayOf(String::class.java, Int::class.java)
        val sourceTypes = listOf("Int", "String")

        emptyImports.match(methodTypes, sourceTypes) shouldBe false
    }

    @Test
    fun `plus operator combines imports`() {
        val imports1 = Imports(setOf(String::class.java), emptySet(), declaringClass)
        val imports2 = Imports(setOf(Int::class.java), emptySet(), declaringClass)
        val combined = imports1 + imports2

        combined.match(arrayOf(String::class.java), listOf("String")) shouldBe true
        combined.match(arrayOf(Int::class.java), listOf("Int")) shouldBe true
    }
}