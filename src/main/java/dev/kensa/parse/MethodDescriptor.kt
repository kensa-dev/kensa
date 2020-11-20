package dev.kensa.parse

import java.lang.reflect.Method

class MethodDescriptor(val name: String, val method: Method, val isSentenceValue: Boolean, val isHighlighted: Boolean)