package dev.kensa.parse

import dev.kensa.kotest.shouldBe
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.util.allProperties
import dev.kensa.util.findMethod
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

fun KClass<*>.propertyNamed(name: String): KProperty<*> = allProperties.find { it.name == name } ?: throw IllegalArgumentException("Property $name not found in class ${this.qualifiedName}")

internal fun assertMethodDescriptors(methods: Map<String, MethodAccessor>, clazz: Class<*>) {
    assertSoftly(methods["method1"]) {
        shouldNotBeNull()
        asClue { shouldBe(MethodAccessor(clazz.findMethod("method1"))) }
    }
}

internal fun assertPropertyDescriptors(properties: Map<String, PropertyAccessor>, clazz: Class<*>) {
    with(properties) {
        assertSoftly(get("field1")) {
            shouldNotBeNull()
            asClue { shouldBe(PropertyAccessor(clazz.kotlin.propertyNamed("field1"))) }
        }
        assertSoftly(get("field2")) {
            shouldNotBeNull()
            asClue { shouldBe(PropertyAccessor(clazz.kotlin.propertyNamed("field2"))) }
        }
        assertSoftly(get("field3")) {
            shouldNotBeNull()
            asClue { shouldBe(PropertyAccessor(clazz.kotlin.propertyNamed("field3"))) }
        }
    }
}