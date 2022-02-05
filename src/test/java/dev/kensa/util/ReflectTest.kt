package dev.kensa.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ReflectTest {

    @Test
    internal fun `can find actual declaring Java interface of a method`() {
        val method = SomeJavaSubClass::class.java.findMethod("overrideMe")

        assertThat(method.actualDeclaringClass()).isEqualTo(SomeJavaInterface::class.java)
    }

    @Test
    internal fun `can find actual declaring Kotlin interface of a method`() {
        val method = SomeKotlinSubClass::class.java.findMethod("overrideMe")

        assertThat(method.actualDeclaringClass()).isEqualTo(SomeKotlinInterface::class.java)
    }

    @Test
    internal fun `can find actual declaring Java class of a method`() {
        val method = SomeJavaSubClass::class.java.findMethod("aSuperMethod")

        assertThat(method.actualDeclaringClass()).isEqualTo(SomeJavaSuperClass::class.java)
    }

    @Test
    internal fun `can find actual declaring Kotlin class of a method`() {
        val method = SomeKotlinSubClass::class.java.findMethod("aSuperFunction")

        assertThat(method.actualDeclaringClass()).isEqualTo(SomeKotlinSuperClass::class.java)
    }

    @Test
    internal fun `can identify kotlin and java classes`() {
        assertThat(SomeJavaSubClass::class.isKotlinClass).isFalse
        assertThat(SomeKotlinSubClass::class.isKotlinClass).isTrue
    }

    @Test
    internal fun `can get a private field from a simple java object`() {
        val privateValue = "A Value"
        val target = SomeJavaSubClass(10, privateValue)

        assertThat(target.fieldValue<String>("field1")).isEqualTo(privateValue)
    }

    @Test
    internal fun `can get a property from a simple kotlin object`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(target.invokeMethod<String>("aProperty")).isEqualTo(value)
    }

    @Test
    internal fun `can get a private property from a simple kotlin object`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(target.invokeMethod<String>("aPrivateProperty")).isEqualTo(value)
    }

    @Test
    internal fun `can get a private field from a simple kotlin object`() {
        val privateValue = "A Value"
        val target = SomeKotlinSubClass(10, privateValue)

        assertThat(target.fieldValue<String>("field1")).isEqualTo(privateValue)
    }

    @Test
    internal fun `can get a private field from a super java object`() {
        val superField = 66
        val target = SomeJavaSubClass(superField, "A Value")

        assertThat(target.fieldValue<Int>("superField")).isEqualTo(superField)
    }

    @Test
    internal fun `can get a private field from a super kotlin object`() {
        val superField = 66
        val target = SomeKotlinSubClass(superField, "A Value")

        assertThat(target.fieldValue<Int>("superField")).isEqualTo(superField)
    }

    @Test
    internal fun `throws on attempt to access non existent field`() {
        assertThatThrownBy { SomeJavaSubClass(10, "").fieldValue<String>("foo") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    internal fun `throws on attempt to invoke non existent method`() {
        assertThatThrownBy { SomeJavaSubClass(10, "").invokeMethod<String>("foo") }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    internal fun `can get value of field via supplier when field type is java supplier`() {
        val suppliedValue = "A Value"
        val target = SomeJavaSubClass(10, suppliedValue)
        assertThat(target.fieldValue<String>("valueSupplier")).isEqualTo(suppliedValue)
    }

    @Test
    internal fun `can get value of field via lambda when field type is lambda`() {
        val suppliedValue = "A Value"
        val target = SomeKotlinSubClass(10, suppliedValue)
        assertThat(target.fieldValue<String>("valueSupplier")).isEqualTo(suppliedValue)
    }

    @Test
    internal fun `can invoke a method on a java object`() {
        val value = "A Value"
        val target = SomeJavaSubClass(10, value)
        assertThat(target.invokeMethod<String>("aMethod"))
    }

    @Test
    internal fun `can invoke a super method on a java object`() {
        val value = 10
        val target = SomeJavaSubClass(value, "foo")
        assertThat(target.invokeMethod<Int>("aSuperMethod"))
    }

    @Test
    internal fun `can invoke a function on a kotlin object`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)
        assertThat(target.invokeMethod<String>("aFunction"))
    }

    @Test
    internal fun `can invoke a super function on a kotlin object`() {
        val value = 10
        val target = SomeKotlinSubClass(value, "foo")
        assertThat(target.invokeMethod<Int>("aSuperFunction"))
    }

    @Test
    internal fun `can invoke an overridden interface method on a java object`() {
        val value = "A Value"
        val target = SomeJavaSubClass(10, value)

        assertThat(target.invokeMethod<String>("overrideMe")).isEqualTo(value)
    }

    @Test
    internal fun `can invoke an overridden interface method on a kotlin object`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(target.invokeMethod<String>("overrideMe")).isEqualTo(value)
    }

    @Test
    internal fun `can invoke a default method on a java interface`() {
        assertThat(SomeJavaSubClass(10, "foo").invokeMethod<String>("aDefaultMethod")).isEqualTo("DefaultValue")
    }

    @Test
    internal fun `can invoke a default function on a kotlin interface`() {
        assertThat(SomeKotlinSubClass(10, "foo").invokeMethod<String>("aDefaultFunction")).isEqualTo("DefaultValue")
    }

    @Test
    internal fun `can get fields of a java class`() {
        assertThat(SomeJavaSubClass::class.java.allFields()).extracting("name").containsExactlyInAnyOrder("field1", "valueSupplier", "superField")
    }

    @Test
    internal fun `can get fields of a kotlin class`() {
        assertThat(SomeKotlinSubClass::class.java.allFields()).extracting("name").containsExactlyInAnyOrder("valueSupplier", "aProperty", "field1", "superField")
    }
}