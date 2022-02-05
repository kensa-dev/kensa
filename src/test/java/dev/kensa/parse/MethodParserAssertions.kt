package dev.kensa.parse

import dev.kensa.util.findMethod
import dev.kensa.util.findRequiredField
import org.assertj.core.api.Assertions.assertThat

object MethodParserAssertions {

    internal fun assertMethodDescriptors(methods: Map<String, MethodDescriptor>, clazz: Class<*>) {
        assertThat(methods).containsEntry(
            "method1",
            MethodDescriptor("method1", clazz.findMethod("method1"), isSentenceValue = true, isHighlighted = false)
        )
    }

    internal fun assertFieldDescriptors(fields: Map<String, FieldDescriptor>, clazz: Class<*>) {
        assertThat(fields).containsEntry(
            "field1",
            FieldDescriptor(
                "field1",
                clazz.findRequiredField("field1"),
                isSentenceValue = false,
                isHighlighted = false,
                isScenario = false
            )
        )
        assertThat(fields).containsEntry(
            "field2",
            FieldDescriptor(
                "field2",
                clazz.findRequiredField("field2"),
                isSentenceValue = false,
                isHighlighted = false,
                isScenario = true
            )
        )
        assertThat(fields).containsEntry(
            "field3",
            FieldDescriptor(
                "field3",
                clazz.findRequiredField("field3"),
                isSentenceValue = true,
                isHighlighted = true,
                isScenario = false
            )
        )
    }
}