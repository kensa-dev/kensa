package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@kotlin.annotation.Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION)
annotation class SentenceValue