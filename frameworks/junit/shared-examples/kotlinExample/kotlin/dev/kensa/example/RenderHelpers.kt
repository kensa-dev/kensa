package dev.kensa.example

import dev.kensa.RenderedValue
import dev.kensa.junit.KensaTest

enum class Product {
    Fttc, Fttp;

    val stringValue: String = name
}

data class Wrapped(val stringValue: String)

object RenderHelpers {
    @RenderedValue
    fun KensaTest.productFor(value: String): Product = if (value == "f") Product.Fttc else Product.Fttp

    @RenderedValue
    fun KensaTest.wrappedFor(value: String): Wrapped = Wrapped("obj-$value")
}
