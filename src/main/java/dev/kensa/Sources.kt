package dev.kensa

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@Retention(RUNTIME)
@Target(CLASS)
annotation class Sources(vararg val value: KClass<*>)