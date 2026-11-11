package dev.kensa.example

import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.ExpandableRenderedValue
import dev.kensa.ExpandableSentence
import dev.kensa.FixturesAndOutputs
import dev.kensa.Highlight
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.WithFixturesAndOutputs
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.fixture.fixture
import dev.kensa.outputs.capturedOutput

@Suppress(
    "RemoveCurlyBracesFromTemplate",
    "RemoveToStringInStringTemplate",
    "REDUNDANT_CALL_OF_CONVERSION_METHOD",
    "unused",
)
class KotlinWithStringInterpolation : WithFixturesAndOutputs {

    val simpleField = "two three"

    @RenderedValue
    val renderedSimpleField = "two three"

    @Highlight
    val highlightedSimpleField = "two three"

    @ExpandableRenderedValue
    val expandableSimpleField = "two three"

    fun simpleFunction() = "two three"

    @ExpandableRenderedValue
    fun expandableSimpleFunction() = "two three"

    @RenderedValue
    fun renderedSimpleFunction() = "two three"

    @ExpandableSentence
    fun expandableSentence() {
        assertThat(simpleField).isEqualTo(simpleField)
    }

    private val myFixtureFx = fixture("myFixture") { "two three" }
    private val myCapturedOutputCo = capturedOutput<String>("myCapturedOutput")

    override val fixturesAndOutputs: FixturesAndOutputs get() = testContext().fixturesAndOutputs

    fun lineStringReferenceTest(normalParam: String, @RenderedValue renderedParam: String, @Highlight highlightedParam: String) {
        assertThat(theStringResult()).isEqualTo("one $simpleField four")
        assertThat(theStringResult()).isEqualTo("one $renderedSimpleField four")
        assertThat(theStringResult()).isEqualTo("one $highlightedSimpleField four")
        assertThat(theStringResult()).isEqualTo("one $expandableSimpleField four")
        assertThat(theStringResult()).isEqualTo("one $normalParam four")
        assertThat(theStringResult()).isEqualTo("one $renderedParam four")
        assertThat(theStringResult()).isEqualTo("one $highlightedParam four")
        assertThat(theStringResult()).isEqualTo("one $STRING_CONSTANT four")
    }

    fun simpleLineStringExpressionTest(normalParam: String, @RenderedValue renderedParam: String, @Highlight highlightedParam: String) {
        assertThat(theStringResult()).isEqualTo("one ${simpleField} four")
        assertThat(theStringResult()).isEqualTo("one ${renderedSimpleField} four")
        assertThat(theStringResult()).isEqualTo("one ${highlightedSimpleField} four")
        assertThat(theStringResult()).isEqualTo("one ${expandableSimpleField} four")
        assertThat(theStringResult()).isEqualTo("one ${normalParam} four")
        assertThat(theStringResult()).isEqualTo("one ${renderedParam} four")
        assertThat(theStringResult()).isEqualTo("one ${highlightedParam} four")
        assertThat(theStringResult()).isEqualTo("one ${STRING_CONSTANT} four")
    }

    fun methodCallsInLineStringExpressionTest(normalParam: String, @RenderedValue renderedParam: String, @Highlight highlightedParam: String) {
        assertThat(theStringResult()).isEqualTo("one ${simpleFunction()} four")
        assertThat(theStringResult()).isEqualTo("one ${expandableSimpleFunction()} four")
        assertThat(theStringResult()).isEqualTo("one ${renderedSimpleFunction()} four")
        assertThat(theStringResult()).isEqualTo("one ${expandableSentence()} four")
        assertThat(theStringResult()).isEqualTo("one ${fixtures(myFixtureFx)} four")
        assertThat(theStringResult()).isEqualTo("one ${outputs(myCapturedOutputCo)} four")
        assertThat(theStringResult()).isEqualTo("one ${outputs[myCapturedOutputCo]} four")
    }

    fun chainedMethodCallsInLineStringExpressionTest(normalParam: String, @RenderedValue renderedParam: String, @Highlight highlightedParam: String) {
        assertThat(theStringResult()).isEqualTo("one ${simpleFunction().toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${expandableSimpleFunction().toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${renderedSimpleFunction().toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${expandableSentence().toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${fixtures(myFixtureFx).toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${outputs(myCapturedOutputCo).toString()} four")
        assertThat(theStringResult()).isEqualTo("one ${outputs[myCapturedOutputCo].toString()} four")
    }

    private fun theStringResult() = StateCollector { "one two three four" }

    companion object {
        private const val STRING_CONSTANT = "a string constant value"
    }
}