package dev.kensa

import dev.kensa.util.Reflect
import kotlin.reflect.KClass

enum class Language(val sourceFileExtension: String) {
    Java("java"),
    Kotlin("kt");

    companion object {
        fun of(kClass: KClass<*>): Language = if (Reflect.isKotlinClass(kClass)) Kotlin else Java
    }
}