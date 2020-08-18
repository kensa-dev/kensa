package dev.kensa.parse

import java.lang.reflect.Field

class FieldDescriptor(val name: String, val field: Field, val isSentenceValue: Boolean, val isHighlighted: Boolean, val isScenario: Boolean)