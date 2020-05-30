package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@kotlin.annotation.Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER)
annotation class Scenario