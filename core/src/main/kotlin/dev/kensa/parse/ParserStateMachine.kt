package dev.kensa.parse

import dev.kensa.parse.Event.*
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.ChainedCallExpression.Type.*
import dev.kensa.parse.State.*
import dev.kensa.parse.state.Matcher
import dev.kensa.parse.state.StateMachine
import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceBuilder

class ParserStateMachine(private val createSentenceBuilder: (Location) -> SentenceBuilder) {

    private val _sentences: MutableList<Sentence> = ArrayList()
    val sentences: List<Sentence>
        get() = _sentences

    private lateinit var sentenceBuilder: SentenceBuilder
    private var lastLocation: Location? = null

    private fun beginSentence(location: Location) {
        sentenceBuilder = createSentenceBuilder(lastLocation ?: location)
    }

    private fun finishSentence() {
        lastLocation = sentenceBuilder.lastLocation
        _sentences += sentenceBuilder.build()
    }

    internal val stateMachine: StateMachine<State, Event> = aStateMachine {

        initialState = Start

        state<Start> {
            on<EnterMethod> { _, event ->
                InMethod
            }
        }
        state<InMethod> {
            on<ExitMethod>(transitionTo(End))
            on<EnterBlock> { currentState, event ->
                TestBlock(currentState)
            }
            on<EnterExpression> { currentState, event ->
                ExpressionFn(currentState)
            }
            ignoreAll<Event> {
                add(Matcher.any<Operator>())
                add(Matcher.any<Terminal>())
            }
        }
        state<ExpressionFn> {
            on<EnterExpression> { currentState, event ->
                ExpressionFn(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                ExpressionFn(currentState)
            }
            on<ExitExpression> { currentState, event ->
                currentState.parentState
            }
            on<EnterStatement> { currentState, event ->
                beginSentence(event.location)
                InStatement(currentState)
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
                add(Matcher.any<Identifier>())
                add(Matcher.any<Field>())
                add(Matcher.any<EnterLambda>())
                add(Matcher.any<ExitLambda>())
            }
        }
        state<TestBlock> {
            on<ExitBlock> { currentState, event -> currentState.parentState }

            on<EnterStatement> { currentState, event ->
                beginSentence(event.location)
                InStatement(currentState)
            }

            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
            }
        }
        state<InStatement> {
            on<ExitStatement> { currentState, event ->
                if (currentState.didBegin) finishSentence()
                currentState.parentState
            }
            on<EnterExpression> { currentState, event ->
                InExpression(currentState)
            }
            on<EnterMethodInvocation> { currentState, event ->
                InMethodInvocation(currentState)
            }
            on<Operator> { currentState, event ->
                sentenceBuilder.appendOperator(event.location, event.text)
                currentState
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.appendIdentifier(event.location, event.name, event.emphasis)
                currentState
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                when (event.type) {
                    Method -> sentenceBuilder.appendMethodValue(event.location, event.name, event.path)
                    Field -> sentenceBuilder.appendFieldValue(event.location, event.name, event.path)
                    Parameter -> sentenceBuilder.appendParameterValue(event.location, event.name, event.path)
                }

                InChainedCallExpression(currentState)
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
            }
        }
        state<InFixturesExpression> {
            on<ExitExpression> { currentState, event ->
                currentState.parentState
            }
            on<FixturesExpression> { currentState, event ->
                InFixturesExpression(currentState)
            }
            on<EnterExpression> { currentState, event ->
                InFixturesExpression(currentState)
            }
            ignoreAll<Event> {
                add(Matcher.any<MethodName>())
                add(Matcher.any<Terminal>())
                add(Matcher.any<Identifier>())
            }
        }
        state<InChainedCallExpression> {
            on<ExitExpression> { currentState, event ->
                currentState.parentState
            }
            on<ChainedCallExpression> { currentState, event ->
                InChainedCallExpression(currentState)
            }
            ignoreAll<Event> {
                add(Matcher.any<Method>())
                add(Matcher.any<Terminal>())
                add(Matcher.any<Identifier>())
                add(Matcher.any<Parameter>())
                add(Matcher.any<Field>())
            }
        }
        state<InLambda> {
            on<ExitLambda> { currentState, event ->
                currentState.parentState
            }
            on<EnterStatement> { currentState, event ->
                InStatement(currentState, didBegin = false)
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
            }
        }
        state<InExpression> {
            on<EnterLambda> { currentState, event ->
                InLambda(currentState)
            }
            on<EnterStatement> { currentState, event ->
                InStatement(currentState)
            }
            on<EnterExpression> { currentState, event ->
                InExpression(currentState)
            }
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<ChainedCallExpression> { currentState, event ->
                when (event.type) {
                    Method -> sentenceBuilder.appendMethodValue(event.location, event.name, event.path)
                    Field -> sentenceBuilder.appendFieldValue(event.location, event.name, event.path)
                    Parameter -> sentenceBuilder.appendParameterValue(event.location, event.name, event.path)
                }

                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<MethodName> { currentState, event ->
                sentenceBuilder.appendIdentifier(event.location, event.name, event.emphasis)
                currentState
            }
            on<EnterTypeArguments> { currentState, event ->
                InTypeArguments(currentState)
            }
            on<Parameter> { currentState, event ->
                sentenceBuilder.appendParameterValue(event.location, event.name, "")
                currentState
            }
            on<Field> { currentState, event ->
                sentenceBuilder.appendFieldValue(event.location, event.name, "")
                currentState
            }
            on<Method> { currentState, event ->
                sentenceBuilder.appendMethodValue(event.location, event.name, "")
                currentState
            }
            on<Nested> { currentState, event ->
                sentenceBuilder.appendNested(event.location, event.name, event.sentences)
                currentState
            }
            on<CharacterLiteral> { currentState, event ->
                sentenceBuilder.appendCharacterLiteral(event.location, event.value)
                currentState
            }
            on<NullLiteral> { currentState, event ->
                sentenceBuilder.appendNullLiteral(event.location)
                currentState
            }
            on<BooleanLiteral> { currentState, event ->
                sentenceBuilder.appendBooleanLiteral(event.location, event.value)
                currentState
            }
            on<StringLiteral> { currentState, event ->
                sentenceBuilder.appendStringLiteral(event.location, event.value)
                currentState
            }
            on<MultilineString> { currentState, event ->
                sentenceBuilder.appendTextBlock(event.value)
                currentState
            }
            on<NumberLiteral> { currentState, event ->
                sentenceBuilder.appendNumberLiteral(event.location, event.value)
                currentState
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.appendIdentifier(event.location, event.name, event.emphasis)
                currentState
            }
            on<Operator> { currentState, event ->
                sentenceBuilder.appendOperator(event.location, event.text)
                currentState
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
            }
        }
        state<InTypeArguments> {
            on<ExitTypeArguments> { currentState, event ->
                currentState.parentState
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
                add(Matcher.any<Identifier>())
            }
        }
        state<InMethodInvocation> {
            on<ExitMethodInvocation> { currentState, _ ->
                currentState.parentState.also {
                    if (it is InMethodInvocation && it.didBegin) finishSentence()
                }
            }
            on<ChainedCallExpression> { currentState, event ->
                when (event.type) {
                    Method -> sentenceBuilder.appendMethodValue(event.location, event.name, event.path)
                    Field -> sentenceBuilder.appendFieldValue(event.location, event.name, event.path)
                    Parameter -> sentenceBuilder.appendParameterValue(event.location, event.name, event.path)
                }

                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<MethodName> { currentState, event ->
                sentenceBuilder.appendIdentifier(event.location, event.name, event.emphasis)
                currentState
            }
            on<Method> { currentState, event ->
                sentenceBuilder.appendMethodValue(event.location, event.name, "")
                currentState
            }
            on<EnterExpression> { currentState, event ->
                InExpression(currentState)
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.appendIdentifier(event.location, event.name, event.emphasis)
                currentState
            }
            ignoreAll<Event> {
                add(Matcher.any<Terminal>())
            }
        }
    }

    fun apply(event: Event) {
        stateMachine.apply(event)
    }
}