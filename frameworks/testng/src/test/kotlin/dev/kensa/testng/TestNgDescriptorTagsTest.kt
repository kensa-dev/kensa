package dev.kensa.testng

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.testng.annotations.Test as TestNgTest

class TestNgDescriptorTagsTest {

    @TestNgTest(groups = ["smoke", "regression"])
    class MethodOnlyGroupsClass {
        @TestNgTest(groups = ["fast"])
        fun methodWithExtraGroup() = Unit
    }

    @TestNgTest(groups = ["api", "smoke"])
    class ClassGroupsOnlyClass {
        @TestNgTest
        fun bareMethod() = Unit
    }

    class NoGroupsClass {
        @TestNgTest
        fun bareMethod() = Unit
    }

    @TestNgTest(groups = ["smoke", "regression"])
    class DuplicatedGroupsClass {
        @TestNgTest(groups = ["smoke", "fast"])
        fun overlappingMethod() = Unit
    }

    @Test
    fun `tagsFor method merges class and method groups preserving order`() {
        val method = MethodOnlyGroupsClass::class.java.getDeclaredMethod("methodWithExtraGroup")

        testNgDescriptor.tagsFor(method) shouldContainExactly listOf("smoke", "regression", "fast")
    }

    @Test
    fun `tagsFor class returns only class level groups`() {
        testNgDescriptor.tagsFor(ClassGroupsOnlyClass::class.java) shouldContainExactly listOf("api", "smoke")
    }

    @Test
    fun `tagsFor method with class only groups returns class groups`() {
        val method = ClassGroupsOnlyClass::class.java.getDeclaredMethod("bareMethod")

        testNgDescriptor.tagsFor(method) shouldContainExactly listOf("api", "smoke")
    }

    @Test
    fun `tagsFor returns empty list when no groups present`() {
        val method = NoGroupsClass::class.java.getDeclaredMethod("bareMethod")

        testNgDescriptor.tagsFor(method) shouldBe emptyList()
        testNgDescriptor.tagsFor(NoGroupsClass::class.java) shouldBe emptyList()
    }

    @Test
    fun `tagsFor deduplicates groups appearing on both class and method`() {
        val method = DuplicatedGroupsClass::class.java.getDeclaredMethod("overlappingMethod")

        testNgDescriptor.tagsFor(method) shouldContainExactly listOf("smoke", "regression", "fast")
    }
}
