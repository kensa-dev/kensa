package dev.kensa.example

import dev.kensa.Highlight

@Suppress("unused")
class TheTestClass {
    @field:Highlight
    val prop1 = "prop1"
    val prop2 = null
    val prop3 = 20
    val prop4 = true
    val prop5 = Wrapper("MyString")

    @Highlight
    fun method1() = "method1"
    fun method2() = null
    fun method3() = 20
    fun method4() = true
    fun method5() = Wrapper("MyString")
}