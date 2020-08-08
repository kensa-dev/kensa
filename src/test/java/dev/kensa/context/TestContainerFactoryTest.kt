package dev.kensa.context

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import kotlin.reflect.jvm.javaMethod

internal class TestContainerFactoryTest {
    private var factory: TestContainerFactory? = null
    private var extensionContext: ExtensionContext? = null

    @BeforeEach
    internal fun setUp() {
        extensionContext = mock(ExtensionContext::class.java)
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
                TestClass::test6.javaMethod
        )
        doReturn(testClass).`when`(extensionContext)!!.requiredTestClass
        val result = factory!!.createFor(extensionContext!!)

        assertThat(result.invocations.values.toList()).extracting("method").containsAll(expected)
    }

    @Suppress("UNUSED_PARAMETER")
    private class TestClass {
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
    }
}