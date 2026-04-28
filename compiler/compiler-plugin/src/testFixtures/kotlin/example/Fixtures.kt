package example

import dev.kensa.ExpandableRenderedValue
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

interface Service {
    fun call(args: Array<Any>)
}

class ExpandableSentences(private val service: Service) {
    @ExpandableSentence
    fun foo(bar: String) {
        service.call(arrayOf(bar))
    }

    @ExpandableSentence
    fun bar(baz: Int) {
        service.call(arrayOf(baz))
    }

    @ExpandableSentence
    fun longParam(value: Long) {
        service.call(arrayOf(value))
    }

    @ExpandableSentence
    fun booleanParam(value: Boolean) {
        service.call(arrayOf(value))
    }

    @ExpandableSentence
    fun doubleParam(value: Double) {
        service.call(arrayOf(value))
    }

    @ExpandableSentence
    fun charParam(value: Char) {
        service.call(arrayOf(value))
    }

    @ExpandableSentence
    fun expressionBody(value: String): Unit = service.call(arrayOf(value))

    @ExpandableSentence
    fun String.onExtensionReceiver() {
        service.call(arrayOf(this))
    }

    context(prefix: String)
    @ExpandableSentence
    fun withContextParameter(suffix: String) {
        service.call(arrayOf(prefix, suffix))
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

    @RenderedValue
    fun unitReturn(value: String) {
        service.call(arrayOf(value))
    }

    @RenderedValue
    fun nullableReturn(value: String): String? {
        service.call(arrayOf(value))
        return value.takeIf { it.isNotEmpty() }
    }

    @RenderedValue
    fun longReturn(value: Long): Long {
        service.call(arrayOf(value))
        return value + 1
    }

    @RenderedValue
    fun booleanReturn(value: Boolean): Boolean {
        service.call(arrayOf(value))
        return !value
    }

    @RenderedValue
    fun doubleReturn(value: Double): Double {
        service.call(arrayOf(value))
        return value * 2
    }

    @RenderedValue
    fun charReturn(value: Char): Char {
        service.call(arrayOf(value))
        return value.uppercaseChar()
    }

    @ExpandableRenderedValue
    fun expandable(value: String): String {
        service.call(arrayOf(value))
        return "expandable-$value"
    }

    @RenderedValue
    fun Int.onExtensionReceiver(): Int = (this * 2).also { service.call(arrayOf(it)) }

    context(prefix: String)
    @RenderedValue
    fun withContextParameter(value: String): String {
        service.call(arrayOf(prefix, value))
        return "$prefix-$value"
    }
}
