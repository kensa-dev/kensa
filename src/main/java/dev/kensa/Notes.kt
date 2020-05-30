package dev.kensa

import kotlin.annotation.AnnotationTarget.*

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(ANNOTATION_CLASS, CLASS, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER)
annotation class Notes(val value: String)