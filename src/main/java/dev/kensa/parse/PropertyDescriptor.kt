package dev.kensa.parse

import kotlin.reflect.KProperty1

class PropertyDescriptor(val name: String, val property: KProperty1<out Any?, Any?>, val isSentenceValue: Boolean, val isHighlighted: Boolean, val isScenario: Boolean) {
}