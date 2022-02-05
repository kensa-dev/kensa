package dev.kensa.util

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ReflectTest {

    @Test
    internal fun `can identify kotlin and java classes`() {
        assertThat(SomeJavaSubClass::class.isKotlinClass).isFalse
        assertThat(SomeKotlinSubClass::class.isKotlinClass).isTrue
    }

    @Test
    internal fun `can get a private field from a simple java class`() {
        val privateValue = "A Value"
        val target = SomeJavaSubClass(10, privateValue)

        assertThat(Reflect.fieldValue<String>("field1", target)).isEqualTo(privateValue)
    }

    @Test
    internal fun `can get a property from a simple kotlin class`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(Reflect.invokeMethod<String>("aProperty", target)).isEqualTo(value)
    }

    @Test
    internal fun `can get a private property from a simple kotlin class`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(Reflect.invokeMethod<String>("aPrivateProperty", target)).isEqualTo(value)
    }

    @Test
    internal fun `can get a private field from a simple kotlin class`() {
        val privateValue = "A Value"
        val target = SomeKotlinSubClass(10, privateValue)

        assertThat(Reflect.fieldValue<String>("field1", target)).isEqualTo(privateValue)
    }

    @Test
    internal fun `can get a private field from a super java class`() {
        val superField = 66
        val target = SomeJavaSubClass(superField, "A Value")

        assertThat(Reflect.fieldValue<Int>("superField", target)).isEqualTo(superField)
    }

    @Test
    internal fun `can get a private field from a super kotlin class`() {
        val superField = 66
        val target = SomeKotlinSubClass(superField, "A Value")

        assertThat(Reflect.fieldValue<Int>("superField", target)).isEqualTo(superField)
    }

    @Test
    internal fun `throws on attempt to access non existent field`() {
        assertThatThrownBy { Reflect.fieldValue<String>("foo", SomeJavaSubClass(10, "")) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    internal fun `throws on attempt to invoke non existent method`() {
        assertThatThrownBy { Reflect.invokeMethod<String>("foo", SomeJavaSubClass(10, "")) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    internal fun `can get value of field via supplier when field type is java supplier`() {
        val suppliedValue = "A Value"
        val target = SomeJavaSubClass(10, suppliedValue)
        assertThat(Reflect.fieldValue<String>("valueSupplier", target)).isEqualTo(suppliedValue)
    }

    @Test
    internal fun `can get value of field via lambda when field type is lambda`() {
        val suppliedValue = "A Value"
        val target = SomeKotlinSubClass(10, suppliedValue)
        assertThat(Reflect.fieldValue<String>("valueSupplier", target)).isEqualTo(suppliedValue)
    }

    @Test
    internal fun `can invoke a method on a java class`() {
        val value = "A Value"
        val target = SomeJavaSubClass(10, value)
        assertThat(Reflect.invokeMethod<String>("aMethod", target))
    }

    @Test
    internal fun `can invoke a super method on a java class`() {
        val value = 10
        val target = SomeJavaSubClass(value, "foo")
        assertThat(Reflect.invokeMethod<Int>("aSuperMethod", target))
    }

    @Test
    internal fun `can invoke a function on a kotlin class`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)
        assertThat(Reflect.invokeMethod<String>("aFunction", target))
    }

    @Test
    internal fun `can invoke a super function on a kotlin class`() {
        val value = 10
        val target = SomeKotlinSubClass(value, "foo")
        assertThat(Reflect.invokeMethod<Int>("aSuperFunction", target))
    }

    @Test
    internal fun `can invoke an overridden interface method on a java class`() {
        val value = "A Value"
        val target = SomeJavaSubClass(10, value)

        assertThat(Reflect.invokeMethod<String>("overrideMe", target)).isEqualTo(value)
    }

    @Test
    internal fun `can invoke an overridden interface method on a kotlin class`() {
        val value = "A Value"
        val target = SomeKotlinSubClass(10, value)

        assertThat(Reflect.invokeMethod<String>("overrideMe", target)).isEqualTo(value)
    }

    @Test
    internal fun `can invoke a default method on a java interface`() {
        assertThat(Reflect.invokeMethod<String>("aDefaultMethod", SomeJavaSubClass(10, "foo"))).isEqualTo("DefaultValue")
    }

    @Test
    internal fun `can invoke a default function on a kotlin interface`() {
        assertThat(Reflect.invokeMethod<String>("aDefaultFunction", SomeKotlinSubClass(10, "foo"))).isEqualTo("DefaultValue")
    }

    @Test
    internal fun `can get fields of a java class`() {
        assertThat(Reflect.fieldsOf(SomeJavaSubClass::class.java)).extracting("name").containsExactlyInAnyOrder("field1", "valueSupplier", "superField")
    }

    @Test
    internal fun `can get fields of a kotlin class`() {
        assertThat(Reflect.fieldsOf(SomeKotlinSubClass::class.java)).extracting("name").containsExactlyInAnyOrder("valueSupplier", "aProperty", "field1", "superField")
    }
}