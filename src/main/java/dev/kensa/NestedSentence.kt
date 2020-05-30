package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@kotlin.annotation.Retention(RUNTIME)
@Target(FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class NestedSentence