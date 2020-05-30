package dev.kensa.state

import dev.kensa.util.Reflect
import java.lang.reflect.Method

class TestInvocationContext(val instance: Any, val method: Method, val arguments: Array<Any?>) {
    fun isKotlin() = Reflect.isKotlinClass(instance::class)
}