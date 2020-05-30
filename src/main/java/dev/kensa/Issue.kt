package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@kotlin.annotation.Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class Issue(vararg val value: String)