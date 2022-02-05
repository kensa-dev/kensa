package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Retention(RUNTIME)
@Target(CLASS, VALUE_PARAMETER)
annotation class CapturedParameter(val value: Boolean = true)