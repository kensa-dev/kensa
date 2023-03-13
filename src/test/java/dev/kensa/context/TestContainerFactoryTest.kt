package dev.kensa.context

import dev.kensa.Kensa
import dev.kensa.output.DefaultTestWriter
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.maps.shouldNotBeEmpty
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.reflect.jvm.javaMethod

internal class TestContainerFactoryTest {
    private lateinit var factory: TestContainerFactory
    private lateinit var extensionContext: ExtensionContext

    @BeforeEach
    internal fun setUp() {
        extensionContext = mock()
        factory = TestContainerFactory()
    }

    @Test
    internal fun createsTestContainerWithPredictableIterationOrderOfInvocationData() {
        val testClass: Class<*> = TestClass::class.java
        val expected = listOf(
            TestClass::test1.javaMethod,
            TestClass::test2.javaMethod,
            TestClass::test3.javaMethod,
            TestClass::test4.javaMethod,
            TestClass::test5.javaMethod,
            TestClass::test6.javaMethod,
            TestClass::test7.javaMethod,
            TestInterface::interfaceTest.javaMethod
        )
        whenever(extensionContext.requiredTestClass).thenReturn(testClass)
        val result = factory.createFor(extensionContext, DefaultTestWriter(Kensa.configuration))

        result.invocations.values.map { it.method } shouldContainAll expected
    }

    @Test
    internal fun `derives display name for internal method`() {
        val testClass: Class<*> = TestClass::class.java

        whenever(extensionContext.requiredTestClass).thenReturn(testClass)
        val result = factory.createFor(extensionContext, DefaultTestWriter(Kensa.configuration))

        result.invocations.filterValues { it.displayName == "Test 7" }.shouldNotBeEmpty()
    }

    private interface TestInterface {
        @Test
        fun interfaceTest() {
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private class TestClass : TestInterface {
        @Test
        fun test1() {
        }

        @Test
        fun test2() {
        }

        @ParameterizedTest
        fun test3(foo: String?) {
        }

        @Test
        fun test4() {
        }

        @ParameterizedTest
        fun test5(foo: String?) {
        }

        @Test
        fun test6() {
        }

        @Test
        internal fun test7() {
        }
    }
}