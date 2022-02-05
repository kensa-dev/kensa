package dev.kensa.state

import dev.kensa.util.isKotlinClass
import java.lang.reflect.Method

class TestInvocationContext(val instance: Any, val method: Method, val arguments: Array<Any?>) {
    val isKotlin get() = instance::class.isKotlinClass
}