package dev.kensa.parse

import java.lang.reflect.Method

data class MethodDescriptor(
    val name: String,
    val method: Method,
    val isSentenceValue: Boolean,
    val isHighlighted: Boolean
)