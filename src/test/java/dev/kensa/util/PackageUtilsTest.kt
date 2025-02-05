package dev.kensa.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PackageUtilsTest {
    
    @Nested
    inner class EmptyPackageList {
        @Test
        internal fun `throws when no packages are provided`() {
            assertThrows<Exception> {
                findCommonPackage(emptyList())
            }
        }
    }
    
    @Nested
    inner class SingleLevelPackages {
        @Test
        internal fun `returns the same package when only one is provided`() {
            findCommonPackage(listOf("foo")) shouldBe "foo"
        }
        
        @Test
        internal fun `returns the same package when all are identical`() {
            findCommonPackage(listOf("foo", "foo", "foo")) shouldBe "foo"
        }

        @Test
        internal fun `returns empty string when there is no common package`() {
            findCommonPackage(listOf("foo", "bar")) shouldBe ""
        }
    }
    
    @Nested
    inner class MultiLevelPackages {
        @Test
        internal fun `returns the same package when only one is provided`() {
            findCommonPackage(listOf("aaa.bbb.ccc")) shouldBe "aaa.bbb.ccc"
        }
        
        @Test
        internal fun `returns the same package when all are identical`() {
            findCommonPackage(listOf("aaa.bbb.ccc", "aaa.bbb.ccc", "aaa.bbb.ccc")) shouldBe "aaa.bbb.ccc"
        }

        @Test
        internal fun `returns empty string when there is no common package`() {
            findCommonPackage(listOf("aaa1.bbb.ccc", "aaa2.bbb.ccc", "aaa3.bbb.ccc")) shouldBe ""
        }
        
        @Test
        internal fun `returns common package when it is the root package`() {
            findCommonPackage(listOf("aaa", "aaa.bbb1.ccc", "aaa.bbb2.ccc", "aaa.bbb2.ccc", "aaa")) shouldBe "aaa"
        }
        
        @Test
        internal fun `returns common package when it is a second-level package with multiple distinct children`() {
            findCommonPackage(listOf("aaa.bbb.ccc1", "aaa.bbb.ccc2", "aaa.bbb.ccc3", "aaa.bbb")) shouldBe "aaa.bbb"
        }
        
        @Test
        internal fun `returns common package when it is a second-level package with only one child`() {
            findCommonPackage(listOf("aaa.bbb.ccc", "aaa.bbb", "aaa.bbb")) shouldBe "aaa.bbb"
        }
        
        @Test
        internal fun `returns common package when it is a third-level package`() {
            findCommonPackage(listOf("aaa.bbb.ccc.ddd1", "aaa.bbb.ccc.ddd2", "aaa.bbb.ccc.ddd3", "aaa.bbb.ccc")) shouldBe "aaa.bbb.ccc"
        }
    }
}