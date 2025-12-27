package dev.kensa.example

import kotlin.annotation.AnnotationTarget.*

@Target(CLASS, PROPERTY, FIELD, PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class TestAnnotation(val value: String)

interface AnnotatedInterface {
    @TestAnnotation("from-interface")
    val property: String
}

@TestAnnotation("on-super")
open class AnnotatedSuper : AnnotatedInterface {
    override val property: String = "val"
}

@TestAnnotation("on-sub")
class AnnotatedSub : AnnotatedSuper()
