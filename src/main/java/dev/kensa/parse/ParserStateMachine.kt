package dev.kensa.parse

import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Event.*
import dev.kensa.parse.Event.LiteralEvent.NumberLiteralEvent
import dev.kensa.parse.Event.LiteralEvent.StringLiteralEvent
import dev.kensa.parse.State.*
import dev.kensa.parse.state.Matcher
import dev.kensa.parse.state.StateMachine
import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceBuilder
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode

class ParserStateMachine(
    private val dictionary: Dictionary,
    private val properties: Map<String, ValueAccessor>,
    private val methods: Map<String, MethodAccessor>,
    private val parameters: Map<String, ParameterAccessor> = emptyMap(),
    private val nestedMethods: Map<String, List<Sentence>> = emptyMap(),
    private val emphasisedMethods: Map<String, EmphasisDescriptor> = emptyMap()
) {

    private val _sentences: MutableList<Sentence> = ArrayList()
    val sentences: List<Sentence>
        get() = _sentences

    private lateinit var sentenceBuilder: SentenceBuilder
    private var lastLocation: Location? = null

    private fun beginSentence(location: Location) {
        sentenceBuilder = SentenceBuilder(lastLocation ?: location, dictionary)
    }

    private fun finishSentence() {
        lastLocation = sentenceBuilder.lastLocation
        _sentences += sentenceBuilder.build()
    }

    private val stateMachine: StateMachine<State, Event<*>> = aStateMachine {

        initialState = Start

        state<Start> {
            on<EnterTestMethodEvent> { _, event ->
                InTestMethod(event.parseTree, isExpressionFunction(event))
            }
        }
        state<InTestMethod> {
            on<ExitTestMethodEvent>(transitionTo(End))
            on<EnterStatementEvent> { currentState, event ->
                beginSentence(event.location)
                InStatement(event.parseTree, currentState)
            }
            on<EnterMethodInvocationEvent> { currentState, event ->
                when {
                    currentState.isExpressionFunction -> currentState
                    else -> {
                        beginSentence(event.location)
                        InMethodCall(event.parseTree, currentState, didBegin = true)
                    }
                }
            }
            on<OperatorEvent> { currentState, _ ->
                currentState
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
                add(Matcher.any<IdentifierEvent>())
                add(Matcher.any<ExitMethodInvocationEvent>())
            }
        }
        state<InStatement> {
            on<ExitStatementEvent> { _, event ->
                finishSentence()
                InTestMethod(event.parseTree, false)
            }
            on<EnterMethodInvocationEvent> { currentState, event ->
                InMethodCall(event.parseTree, currentState)
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
            }
        }
        state<InMethodCall> {
            on<EnterMethodInvocationEvent> { currentState, event ->
                InMethodCall(event.parseTree, currentState)
            }
            on<ExitMethodInvocationEvent> { currentState, _ ->
                currentState.parentState.also {
                    if (it is InMethodCall && it.didBegin) finishSentence()
                }
            }
            on<OperatorEvent> { currentState, event ->
                sentenceBuilder.appendOperator(event.location, event.parseTree.text)
                currentState
            }
            on<StringLiteralEvent> { currentState, event ->
                sentenceBuilder.appendStringLiteral(event.location, event.value)
                currentState
            }
            on<NumberLiteralEvent> { currentState, event ->
                sentenceBuilder.appendLiteral(event.location, event.parseTree.text)
                currentState
            }
            on<IdentifierEvent> { currentState, event ->
                event.parseTree.run {
                    when {
                        isNestedMethodCall(text) -> {
                            sentenceBuilder.appendNested(
                                event.location, text, nestedMethods[text]
                                    ?: error("Expected nested method sentences to be present")
                            )
                            currentState
                        }

                        isScenarioIdentifier(text) -> {
                            InScenarioCall(this, currentState.parentState, text)
                        }

                        isFieldIdentifier(text) -> {
                            sentenceBuilder.appendFieldIdentifier(event.location, text)
                            currentState
                        }

                        isMethodIdentifier(text) -> {
                            sentenceBuilder.appendMethodIdentifier(event.location, text)
                            currentState
                        }

                        isParameterIdentifier(text) -> {
                            sentenceBuilder.appendParameterIdentifier(event.location, text)
                            currentState
                        }

                        else -> {
                            sentenceBuilder.appendIdentifier(
                                event.location, text, emphasisedMethods[text]
                                    ?: EmphasisDescriptor.Default
                            )
                            currentState
                        }
                    }
                }
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
                add(Matcher.any<EnterStatementEvent>())
                add(Matcher.any<ExitStatementEvent>())
            }
        }
        state<InScenarioCall> {
            on<ExitMethodInvocationEvent> { currentState, _ ->
                currentState.parentState
            }
            on<IdentifierEvent> { currentState, event ->
                sentenceBuilder.appendScenarioIdentifier(event.location, "${currentState.scenarioName}.${event.parseTree.text}")
                currentState
            }
            on<StringLiteralEvent> { currentState, _ ->
                currentState
            }
            on<NumberLiteralEvent> { currentState, _ ->
                currentState
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
            }
        }
    }

    fun transition(event: Event<*>) {
        stateMachine.transition(event)
    }

    private fun isExpressionFunction(event: EnterTestMethodEvent) = event.parseTree.firstChildOrNull()?.startsExpressionFunction() ?: false

    private fun isNestedMethodCall(value: String) = nestedMethods.containsKey(value)

    private fun isScenarioIdentifier(value: String) = properties[value]?.isScenario ?: false

    private fun isFieldIdentifier(value: String) = properties[value]?.run { isSentenceValue || isHighlight } ?: false

    private fun isMethodIdentifier(value: String) = methods[value]?.run { isSentenceValue || isHighlight } ?: false

    private fun isParameterIdentifier(value: String) = parameters[value]?.run { isSentenceValue || isHighlight } ?: false

    private fun ParseTree.firstChildOrNull() = getChild(0)
    private fun ParseTree.startsExpressionFunction() = this is TerminalNode && text == "="
}