package dev.kensa.util

import dev.kensa.example.*
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.reflect.AnnotatedElement
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

internal class ReflectTest {

    @Nested
    inner class AnnotationExtensions {

        @Test
        internal fun `findAnnotationInHierarchy finds annotation on class itself`() {
            AnnotatedSuper::class.java.findAnnotationInHierarchy<TestAnnotation>()?.value shouldBe "on-super"
        }

        @Test
        internal fun `findAnnotationInHierarchy finds annotation on super class`() {
            AnnotatedSub::class.java.findAnnotationInHierarchy<TestAnnotation>()?.value shouldBe "on-sub"
        }

        @Test
        internal fun `findAllAnnotationsInHierarchy collects from everywhere`() {
            val annotations = AnnotatedSub::class.java.findAllAnnotationsInHierarchy<TestAnnotation>()

            annotations.map { it.value }.shouldContainExactlyInAnyOrder("on-sub", "on-super")
        }

        @Test
        internal fun `AnnotatedElement findAnnotation is hierarchy aware for classes`() {
            val element: AnnotatedElement = AnnotatedSub::class.java
            element.findAnnotation<TestAnnotation>()?.value shouldBe "on-sub"
        }

        @Test
        internal fun `AnnotatedElement findAnnotation is standard for non-class elements`() {
            val method = AnnotatedSuper::class.java.getDeclaredMethod("getProperty")
            // Methods don't inherit annotations from overridden methods automatically in standard Java reflection
            method.findAnnotation<TestAnnotation>() shouldBe null
        }

        @Test
        internal fun `KProperty extensions find annotations across Kotlin and Java elements`() {
            val prop = AnnotatedInterface::class.memberProperties.first { it.name == "property" }

            prop.hasKotlinOrJavaAnnotation<TestAnnotation>().shouldBeTrue()
            prop.findKotlinOrJavaAnnotation<TestAnnotation>()?.value shouldBe "from-interface"
        }

        @Test
        internal fun `findAllKotlinOrJavaAnnotations collects repeatable annotations on properties`() {
            // Dummy for repeatable property test
            class RepeatableProp(@field:TestAnnotation("one") @get:TestAnnotation("two") val p: String)

            val prop = RepeatableProp::class.declaredMemberProperties.first()
            val values = prop.findAllKotlinOrJavaAnnotations<TestAnnotation>().map { it.value }

            values.shouldContainExactlyInAnyOrder("one", "two")
        }
    }

    @Nested
    inner class ActualDeclaringClass {

        @Test
        internal fun `can find actual declaring Java interface of a method`() {
            val method = SomeJavaSubClass::class.java.findMethod("overrideMe")

            method.actualDeclaringClass() shouldBe SomeJavaInterface::class.java
        }

        @Test
        internal fun `can find actual declaring Kotlin interface of a method`() {
            val method = SomeKotlinSubClass::class.java.findMethod("overrideMe")

            method.actualDeclaringClass() shouldBe SomeKotlinInterface::class.java
        }

        @Test
        internal fun `can find actual declaring Java class of a method`() {
            val method = SomeJavaSubClass::class.java.findMethod("aSuperMethod")

            method.actualDeclaringClass() shouldBe SomeJavaSuperClass::class.java
        }

        @Test
        internal fun `can find actual declaring Kotlin class of a method`() {
            val method = SomeKotlinSubClass::class.java.findMethod("aSuperFunction")

            method.actualDeclaringClass() shouldBe SomeKotlinSuperClass::class.java
        }
    }

    @Nested
    inner class ClassIdentification {

        @Test
        internal fun `can identify kotlin and java classes`() {
            SomeJavaSubClass::class.isKotlinClass.shouldBeFalse()
            SomeKotlinSubClass::class.isKotlinClass.shouldBeTrue()
        }
    }

    @Nested
    inner class JavaReflection {

        @Test
        internal fun `can get a private field from a simple java object`() {
            val privateValue = "A Value"
            val target = SomeJavaSubClass(10, privateValue)

            target.fieldValue("field1") shouldBe privateValue
        }

        @Test
        internal fun `can get a private field from a super java object`() {
            val superField = 66
            val target = SomeJavaSubClass(superField, "A Value")

            target.fieldValue("superField") shouldBe superField
        }

        @Test
        internal fun `throws on attempt to access non existent field`() {
            shouldThrowExactly<IllegalArgumentException> { SomeJavaSubClass(10, "").fieldValue("foo") }
        }

        @Test
        internal fun `throws on attempt to invoke non existent method`() {
            shouldThrowExactly<IllegalArgumentException> { SomeJavaSubClass(10, "").invokeMethod<String>("foo") }
        }

        @Test
        internal fun `can get value of field via supplier when field type is java supplier`() {
            val suppliedValue = "A Value"
            val target = SomeJavaSubClass(10, suppliedValue)
            target.fieldValue("valueSupplier") shouldBe suppliedValue
        }

        @Test
        internal fun `can invoke a method on a java object`() {
            val value = "A Value"
            val target = SomeJavaSubClass(10, value)
            target.invokeMethod<String>("aMethod") shouldBe value
        }

        @Test
        internal fun `can invoke a super method on a java object`() {
            val value = 10
            val target = SomeJavaSubClass(value, "foo")
            target.invokeMethod<Int>("aSuperMethod") shouldBe value
        }

        @Test
        internal fun `can invoke an overridden interface method on a java object`() {
            val value = "A Value"
            val target = SomeJavaSubClass(10, value)

            target.invokeMethod<String>("overrideMe") shouldBe value
        }

        @Test
        internal fun `can invoke a default method on a java interface`() {
            SomeJavaSubClass(10, "foo").invokeMethod<String>("aDefaultMethod") shouldBe "DefaultValue"
        }

        @Test
        internal fun `can get fields of a java class`() {
            SomeJavaSubClass::class.java.allFields.map { it.name }.shouldContainExactlyInAnyOrder("field1", "valueSupplier", "superField")
        }
    }

    @Nested
    inner class KotlinReflection {

        @Test
        internal fun `can get a property from a simple kotlin object`() {
            val value = "A Value"
            val target = SomeKotlinSubClass(10, value)

            target.invokeMethod<String>("aProperty") shouldBe value
        }

        @Test
        internal fun `can get a private property via invokeMethod from a simple kotlin object`() {
            val value = "A Value"
            val target = SomeKotlinSubClass(10, value)

            target.invokeMethod<String>("aPrivateProperty") shouldBe value
        }

        @Test
        internal fun `can get a private field from a simple kotlin object`() {
            val privateValue = "A Value"
            val target = SomeKotlinSubClass(10, privateValue)

            target.fieldValue("field1") shouldBe privateValue
        }

        @Test
        internal fun `can get a private field from a super kotlin object`() {
            val superField = 66
            val target = SomeKotlinSubClass(superField, "A Value")

            target.fieldValue("superField") shouldBe superField
        }

        @Test
        internal fun `can get value of field via lambda when field type is lambda`() {
            val suppliedValue = "A Value"
            val target = SomeKotlinSubClass(10, suppliedValue)
            target.fieldValue("valueSupplier") shouldBe suppliedValue
        }

        @Test
        internal fun `can invoke a function on a kotlin object`() {
            val value = "A Value"
            val target = SomeKotlinSubClass(10, value)
            target.invokeMethod<String>("aFunction") shouldBe value
        }

        @Test
        internal fun `can invoke a super function on a kotlin object`() {
            val value = 10
            val target = SomeKotlinSubClass(value, "foo")
            target.invokeMethod<Int>("aSuperFunction") shouldBe value
        }

        @Test
        internal fun `can invoke an overridden interface method on a kotlin object`() {
            val value = "A Value"
            val target = SomeKotlinSubClass(10, value)

            target.invokeMethod<String>("overrideMe") shouldBe value
        }

        @Test
        internal fun `can invoke a default function on a kotlin interface`() {
            SomeKotlinSubClass(10, "foo").invokeMethod<String>("aDefaultFunction") shouldBe "DefaultValue"
        }

        @Test
        internal fun `can get fields of a kotlin class`() {
            SomeKotlinSubClass::class.java.allFields.map { it.name }.shouldContainExactlyInAnyOrder("valueSupplier", "aProperty", "field1", "superField")
        }
    }
}