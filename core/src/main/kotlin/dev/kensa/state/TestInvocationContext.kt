package dev.kensa.state

import dev.kensa.util.isKotlinClass
import java.lang.reflect.Method

class TestInvocationContext(val instance: Any, val method: Method, val arguments: Array<Any?>, val displayName: String, val startTimeMs: Long) {
    val isKotlin = instance::class.isKotlinClass
}