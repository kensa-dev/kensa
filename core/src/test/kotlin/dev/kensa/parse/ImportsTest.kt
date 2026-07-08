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
        emptyImports.match(arrayOf(Float::class.java), listOf("Float")) shouldBe true
        emptyImports.match(arrayOf(Float::class.javaObjectType), listOf("Float")) shouldBe true
        emptyImports.match(arrayOf(Short::class.java), listOf("Short")) shouldBe true
        emptyImports.match(arrayOf(Byte::class.java), listOf("Byte")) shouldBe true
        emptyImports.match(arrayOf(Char::class.java), listOf("Char")) shouldBe true
        emptyImports.match(arrayOf(Char::class.javaObjectType), listOf("Char")) shouldBe true
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
    fun `matches kotlin lambda types to FunctionN`() {
        emptyImports.match(arrayOf(Function0::class.java), listOf("() -> Unit")) shouldBe true
        emptyImports.match(arrayOf(Function1::class.java), listOf("(String) -> Boolean")) shouldBe true
        emptyImports.match(arrayOf(Function2::class.java), listOf("(String, Int) -> Boolean")) shouldBe true
    }

    @Test
    fun `matches kotlin lambda types with generic return types`() {
        emptyImports.match(arrayOf(Function0::class.java), listOf("() -> Matcher<*>")) shouldBe true
        emptyImports.match(arrayOf(Function1::class.java), listOf("(String) -> List<Int>")) shouldBe true
    }

    @Test
    fun `matches kotlin lambda with lambda parameter`() {
        emptyImports.match(arrayOf(Function1::class.java), listOf("((Int) -> Boolean) -> Unit")) shouldBe true
    }

    @Test
    fun `does not match non-lambda source types as FunctionN`() {
        emptyImports.match(arrayOf(Function0::class.java), listOf("String")) shouldBe false
    }

    @Test
    fun `plus operator combines imports`() {
        val imports1 = Imports(setOf(String::class.java), emptySet(), declaringClass)
        val imports2 = Imports(setOf(Int::class.java), emptySet(), declaringClass)
        val combined = imports1 + imports2

        combined.match(arrayOf(String::class.java), listOf("String")) shouldBe true
        combined.match(arrayOf(Int::class.java), listOf("Int")) shouldBe true
    }

    @Test
    fun `matches primitive component arrays via kotlin equivalents`() {
        emptyImports.match(arrayOf(IntArray::class.java), listOf("Array<Int>")) shouldBe true
        emptyImports.match(arrayOf(Array<Int>::class.java), listOf("Array<Int>")) shouldBe true
        emptyImports.match(arrayOf(FloatArray::class.java), listOf("Array<Float>")) shouldBe true
        emptyImports.match(arrayOf(LongArray::class.java), listOf("Array<Int>")) shouldBe false
    }

    @Test
    fun `matches kotlin array types with variance`() {
        emptyImports.match(arrayOf(emptyArray<Pair<String, String>>().javaClass), listOf("Array<outPair<String,String>>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("Array<outString>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("Array<inString>")) shouldBe true
        emptyImports.match(arrayOf(emptyArray<Array<String>>().javaClass), listOf("Array<outArray<String>>")) shouldBe true
        emptyImports.match(arrayOf(Array<Any>::class.java), listOf("Array<output>")) shouldBe false
        emptyImports.match(arrayOf(Array<Any>::class.java), listOf("Array<input>")) shouldBe false
    }

    @Test
    fun `matches kotlin object array parameter types`() {
        emptyImports.match(arrayOf(emptyArray<Pair<String, String>>().javaClass), listOf("Array<Pair<String,String>>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("Array<String>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("kotlin.Array<String>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("Array<String?>")) shouldBe true
        emptyImports.match(arrayOf(emptyArray<Array<String>>().javaClass), listOf("Array<Array<String>>")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("Array<Int>")) shouldBe false
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("List<String>")) shouldBe false
        emptyImports.match(arrayOf(String::class.java), listOf("Array<String>")) shouldBe false
    }

    @Test
    fun `matches kotlin primitive array parameter types`() {
        emptyImports.match(arrayOf(IntArray::class.java), listOf("IntArray")) shouldBe true
        emptyImports.match(arrayOf(LongArray::class.java), listOf("LongArray")) shouldBe true
        emptyImports.match(arrayOf(ShortArray::class.java), listOf("ShortArray")) shouldBe true
        emptyImports.match(arrayOf(ByteArray::class.java), listOf("ByteArray")) shouldBe true
        emptyImports.match(arrayOf(DoubleArray::class.java), listOf("DoubleArray")) shouldBe true
        emptyImports.match(arrayOf(FloatArray::class.java), listOf("FloatArray")) shouldBe true
        emptyImports.match(arrayOf(BooleanArray::class.java), listOf("BooleanArray")) shouldBe true
        emptyImports.match(arrayOf(CharArray::class.java), listOf("CharArray")) shouldBe true
        emptyImports.match(arrayOf(IntArray::class.java), listOf("kotlin.IntArray")) shouldBe true
        emptyImports.match(arrayOf(Array<IntArray>::class.java), listOf("Array<IntArray>")) shouldBe true
        emptyImports.match(arrayOf(IntArray::class.java), listOf("LongArray")) shouldBe false
        emptyImports.match(arrayOf(Array<Int>::class.java), listOf("IntArray")) shouldBe false
    }

    @Test
    fun `matches java array parameter types`() {
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("String[]")) shouldBe true
        emptyImports.match(arrayOf(IntArray::class.java), listOf("int[]")) shouldBe true
        emptyImports.match(arrayOf(emptyArray<Array<String>>().javaClass), listOf("String[][]")) shouldBe true
        emptyImports.match(arrayOf(emptyArray<List<String>>().javaClass), listOf("List<String>[]")) shouldBe true
        emptyImports.match(arrayOf(Array<String>::class.java), listOf("int[]")) shouldBe false
        emptyImports.match(arrayOf(String::class.java), listOf("String[]")) shouldBe false
    }
}