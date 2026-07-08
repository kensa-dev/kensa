package example

import dev.kensa.ExpandableRenderedValue
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.PrimaryFixture
import dev.kensa.fixture.fixture

object FactoryFixtures : FixtureContainer {
    @dev.kensa.Fixture("MyFixture")
    fun myFixture(param: String): PrimaryFixture<String> = fixture { if (param == "Meh") "Bah" else "Yay" }

    @dev.kensa.Fixture("TwoArg")
    fun twoArg(first: String, second: Int): PrimaryFixture<String> = fixture { "$first-$second" }
}

interface Service {
    fun call(args: Array<Any>)
}

@JvmInline
value class MyValueClass(val value: String)

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

    @ExpandableSentence
    fun valueClassParam(value: MyValueClass) {
        service.call(arrayOf(value))
    }

    @ExpandableSentence
    fun String.onExtensionReceiverWithValueClassParam(value: MyValueClass) {
        service.call(arrayOf(this, value))
    }

    @ExpandableSentence
    fun MyValueClass.onValueClassExtensionReceiverWithValueClassParam(value: MyValueClass) {
        service.call(arrayOf(this, value))
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

    @RenderedValue
    fun valueClassParam(value: MyValueClass): String {
        service.call(arrayOf(value))
        return "rendered-${value.value}"
    }

    @RenderedValue
    fun valueClassReturn(value: String): MyValueClass {
        service.call(arrayOf(value))
        return MyValueClass("wrapped-$value")
    }
}
