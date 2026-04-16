package dev.kensa.output.json

import dev.kensa.Tab
import dev.kensa.context.TestContainer
import dev.kensa.fixture.Fixtures
import dev.kensa.fixture.FixtureSpec
import dev.kensa.parse.ParseError
import dev.kensa.parse.RenderError
import dev.kensa.render.diagram.SequenceDiagram
import dev.kensa.sentence.RenderedSentence
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.state.TestState
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.lang.reflect.Method
import kotlin.time.Duration

internal fun fakeTestInvocation(
    elapsed: Duration = Duration.ZERO,
    displayName: String = "invocation",
    parameterizedTestDescription: String? = null,
    highlightedValues: Set<NamedValue> = emptySet(),
    sentences: List<RenderedSentence> = emptyList(),
    parameters: Collection<NamedValue> = emptyList(),
    interactions: Set<KensaMap.Entry> = emptySet(),
    outputNamesAndValues: Set<NamedValue> = emptySet(),
    fixturesNamesAndValues: List<NamedValue> = emptyList(),
    fixtureSpecs: List<FixtureSpec> = emptyList(),
    sequenceDiagram: SequenceDiagram? = null,
    state: TestState = TestState.Passed,
    executionException: Throwable? = null,
    renderErrors: List<RenderError> = emptyList(),
): TestInvocation {
    val fixtures = mock<Fixtures> { on { specs() } doReturn fixtureSpecs }
    return mock<TestInvocation> {
        on { it.elapsed } doReturn elapsed
        on { it.displayName } doReturn displayName
        on { it.parameterizedTestDescription } doReturn parameterizedTestDescription
        on { it.highlightedValues } doReturn highlightedValues
        on { it.sentences } doReturn sentences
        on { it.parameters } doReturn parameters
        on { it.interactions } doReturn interactions
        on { it.outputNamesAndValues } doReturn outputNamesAndValues
        on { it.fixturesNamesAndValues } doReturn fixturesNamesAndValues
        on { it.fixtures } doReturn fixtures
        on { it.sequenceDiagram } doReturn sequenceDiagram
        on { it.state } doReturn state
        on { it.executionException } doReturn executionException
        on { it.renderErrors } doReturn renderErrors
    }
}

internal fun fakeTestMethodContainer(
    method: Method,
    displayName: String = method.name,
    issues: List<String> = emptyList(),
    state: TestState = TestState.Passed,
    autoOpenTab: Tab = Tab.None,
    invocations: List<TestInvocation> = emptyList(),
    parseErrors: List<ParseError> = emptyList(),
): TestMethodContainer = mock {
    on { it.method } doReturn method
    on { it.displayName } doReturn displayName
    on { it.issues } doReturn issues
    on { it.state } doReturn state
    on { it.autoOpenTab } doReturn autoOpenTab
    on { it.invocations } doReturn invocations
    on { it.parseErrors } doReturn parseErrors
}

internal fun fakeTestContainer(
    testClass: Class<*>,
    displayName: String = testClass.simpleName,
    state: TestState = TestState.Passed,
    notes: String? = null,
    issues: List<String> = emptyList(),
    methodContainers: List<TestMethodContainer> = emptyList(),
): TestContainer {
    val byMethod = methodContainers.associateBy { it.method }
    return mock {
        on { it.testClass } doReturn testClass
        on { it.displayName } doReturn displayName
        on { it.state } doReturn state
        on { it.notes } doReturn notes
        on { it.issues } doReturn issues
        on { it.methodContainers } doReturn byMethod
        on { it.orderedMethodContainers } doReturn methodContainers
    }
}

internal fun interactionEntry(
    key: String,
    value: Any? = "value",
    timestamp: Long = 0L,
    attributes: Attributes = emptyAttributes(),
): KensaMap.Entry = KensaMap.Entry(key, value, timestamp, attributes)
