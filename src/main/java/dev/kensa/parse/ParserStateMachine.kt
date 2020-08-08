package dev.kensa.parse

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

class ParserStateMachine(
        dictionary: Dictionary,
        private val fields: Map<String, FieldDescriptor>,
        private val parameters: Map<String, ParameterDescriptor> = emptyMap(),
        private val nestedMethods: Map<String, List<Sentence>> = emptyMap()) {

    private val keywords = dictionary.keywords
    private val acronyms = dictionary.acronymStrings
    private val _sentences: MutableList<Sentence> = ArrayList()

    val sentences: List<Sentence>
        get() = _sentences

    private var _sentenceBuilder: SentenceBuilder? = null

    private val sentenceBuilder: SentenceBuilder
        get() = _sentenceBuilder ?: throw IllegalStateException("Expected Sentence Builder to be available - ensure sentence has been started!")

    private fun beginSentence(lineNumber: Int) {
        _sentenceBuilder = SentenceBuilder(lineNumber, keywords, acronyms)
    }

    private fun finishSentence() {
        _sentences += sentenceBuilder.build()
        _sentenceBuilder = null
    }

    private val stateMachine: StateMachine<State, Event<*>> = aStateMachine {

        initialState = Start

        state<Start> {
            on<EnterTestMethod> { _, event ->
                InTestMethod(event.parseTree)
            }
        }
        state<InTestMethod> {
            on<ExitTestMethod>(transitionTo(End))
            on<EnterStatementEvent> { currentState, event ->
                beginSentence(event.lineNumber)
                InStatement(event.parseTree, currentState)
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
            }
        }
        state<InStatement> {
            on<ExitStatementEvent> { _, event ->
                finishSentence()
                beginSentence(event.lineNumber)
                InTestMethod(event.parseTree)
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
                currentState.parentState
            }
            on<StringLiteralEvent> { currentState, event ->
                sentenceBuilder.appendStringLiteral(event.lineNumber, event.value)
                currentState
            }
            on<NumberLiteralEvent> { currentState, event ->
                sentenceBuilder.appendLiteral(event.lineNumber, event.parseTree.text)
                currentState
            }
            on<IdentifierEvent> { currentState, event ->
                event.parseTree.run {
                    when {
                        isNestedMethodCall(text) -> {
                            sentenceBuilder.appendNested(event.lineNumber, text, nestedMethods[text] ?: error("Expected nested method sentences to be present"))
                            currentState
                        }
                        isScenarioIdentifier(text) -> {
                            InScenarioCall(this, currentState.parentState, text)
                        }
                        isFieldIdentifier(text) -> {
                            sentenceBuilder.appendFieldIdentifier(event.lineNumber, text)
                            currentState
                        }
                        isParameterIdentifier(text) -> {
                            sentenceBuilder.appendParameterIdentifier(event.lineNumber, text)
                            currentState
                        }
                        else -> {
                            sentenceBuilder.appendIdentifier(event.lineNumber, text)
                            currentState
                        }
                    }
                }
            }
            ignoreAll<Event<*>> {
                add(Matcher.any<TerminalNodeEvent>())
            }
        }
        state<InScenarioCall> {
            on<ExitMethodInvocationEvent> { currentState, _ ->
                currentState.parentState
            }
            on<IdentifierEvent> { currentState, event ->
                sentenceBuilder.appendScenarioIdentifier(event.lineNumber, "${currentState.scenarioName}.${event.parseTree.text}")
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

    private fun isNestedMethodCall(value: String) = nestedMethods.containsKey(value)

    private fun isScenarioIdentifier(value: String) = fields[value]?.isScenario ?: false

    private fun isFieldIdentifier(value: String) = fields[value]?.run { isSentenceValue || isHighlighted } ?: false

    private fun isParameterIdentifier(value: String) = parameters[value]?.run { isSentenceValue || isHighlighted } ?: false

}