package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER)
annotation class ScenarioHolder