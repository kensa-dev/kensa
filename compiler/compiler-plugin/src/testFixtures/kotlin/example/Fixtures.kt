package example

import dev.kensa.NestedSentence
import dev.kensa.RenderedValue

interface Service {
    fun call(args: Array<Any>)
}

class NestedSentences(private val service: Service) {
    @NestedSentence
    fun foo(bar: String) {
        service.call(arrayOf(bar))
    }

    @NestedSentence
    fun bar(baz: Int) {
        service.call(arrayOf(baz))
    }
}

class RenderedValues(private val service: Service) {

    @RenderedValue
    fun foo(bar: String): String {
        service.call(arrayOf(bar))

        return "foo-$bar"
    }

    @RenderedValue
    fun bar(baz: Int): Int = (baz + 1).also { service.call(arrayOf(it)) }
}