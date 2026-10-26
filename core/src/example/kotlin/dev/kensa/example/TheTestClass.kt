package dev.kensa.example

import dev.kensa.ExpandableRenderedValue
import dev.kensa.Highlight
import dev.kensa.RenderedValueStyle.Tabular

@Suppress("unused")
class TheTestClass {
    @field:Highlight
    val prop1 = "prop1"
    val prop2 = null
    val prop3 = 20
    val prop4 = true
    val prop5 = Wrapper("MyString")
    @ExpandableRenderedValue
    val prop6 = listOf("x", "y")
    @ExpandableRenderedValue(renderAs = Tabular, headers = ["Key", "Value"])
    val prop7 = listOf("a" to 1, "b" to 2)

    @Highlight
    fun method1() = "method1"
    fun method2() = null
    fun method3() = 20
    fun method4() = true
    fun method5() = Wrapper("MyString")
}