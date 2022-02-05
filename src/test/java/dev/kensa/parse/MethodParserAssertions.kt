package dev.kensa.parse

import dev.kensa.util.Reflect
import org.assertj.core.api.Assertions.assertThat
import kotlin.reflect.KClass

object MethodParserAssertions {

    internal fun assertMethodDescriptors(methods: Map<String, MethodDescriptor>, clazz: KClass<*>) {
        assertThat(methods).containsEntry(
            "method1",
            MethodDescriptor("method1", Reflect.findMethod("method1", clazz), true, false)
        )
    }

    internal fun assertFieldDescriptors(fields: Map<String, FieldDescriptor>, clazz: KClass<*>) {
        assertThat(fields).containsEntry(
            "field1",
            FieldDescriptor(
                "field1",
                Reflect.findRequiredField("field1", clazz)!!,
                false,
                false,
                false
            )
        )
        assertThat(fields).containsEntry(
            "field2",
            FieldDescriptor(
                "field2",
                Reflect.findRequiredField("field2", clazz)!!,
                false,
                false,
                true
            )
        )
        assertThat(fields).containsEntry(
            "field3",
            FieldDescriptor(
                "field3",
                Reflect.findRequiredField("field3", clazz)!!,
                true,
                true,
                false
            )
        )
    }
}