package dev.kensa.parse

import dev.kensa.parse.Event.*
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.PathExpression.ChainedCallExpression
import dev.kensa.parse.LocatedEvent.PathExpression.FixturesExpression
import dev.kensa.parse.State.*
import dev.kensa.parse.State.WithAppendable.InNestedWithArguments
import dev.kensa.parse.State.WithAppendable.InNestedWithArgumentsParameter
import dev.kensa.parse.state.Matcher.Companion.any
import dev.kensa.parse.state.StateMachine
import dev.kensa.parse.state.StateMachineBuilder
import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateSentence

class ParserStateMachine(private val createSentenceBuilder: (Location, Location) -> SentenceBuilder) {

    private val _sentences: MutableList<TemplateSentence> = ArrayList()
    val sentences: List<TemplateSentence>
        get() = _sentences

    private lateinit var sentenceBuilder: SentenceBuilder
    private var lastSentenceEndLocation: Location? = null

    private fun beginSentence(location: Location) {
        sentenceBuilder = createSentenceBuilder(location, lastSentenceEndLocation ?: location)
    }

    private fun finishSentence() {
        _sentences += sentenceBuilder.build()
        lastSentenceEndLocation = sentenceBuilder.lastLocation
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
            ignoreAll(any<Operator>(), any<Terminal>())
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
            ignoreAll(
                any<Terminal>(),
                any<Identifier>(),
                any<Field>(),
                any<EnterLambda>(),
                any<ExitLambda>(),
                any<EnterValueArguments>(),
                any<ExitValueArguments>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>()
            )
        }
        state<TestBlock> {
            on<ExitBlock> { currentState, event -> currentState.parentState }

            on<EnterStatement> { currentState, event ->
                beginSentence(event.location)
                InStatement(currentState)
            }

            ignoreAll(any<Terminal>())
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
                sentenceBuilder.append(event)
                currentState
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                sentenceBuilder.append(event)

                InChainedCallExpression(currentState)
            }
            ignoreAll(any<Terminal>())
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
            ignoreAll(any<EnterValueArguments>(),
                any<ExitValueArguments>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>(),
                any<Terminal>(),
                any<Identifier>()
            )
        }
        state<InChainedCallExpression> {
            on<ExitExpression> { currentState, event ->
                currentState.parentState
            }
            on<ChainedCallExpression> { currentState, event ->
                InChainedCallExpression(currentState)
            }
            ignoreAll(any<Method>(),
                any<Terminal>(),
                any<Identifier>(),
                any<Parameter>(),
                any<Field>(),
                any<EnterValueArguments>(),
                any<ExitValueArguments>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>()
            )
        }
        state<InLambda> {
            on<ExitLambda> { currentState, event ->
                currentState.parentState
            }
            on<EnterStatement> { currentState, event ->
                InStatement(currentState, didBegin = false)
            }
            ignoreAll(any<Terminal>())
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
                sentenceBuilder.append(event)
                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<EnterTypeArguments> { currentState, event ->
                InTypeArguments(currentState)
            }
            on<Parameter> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<Field> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<Method> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<Nested> { currentState, event ->
                sentenceBuilder.beginNested(event.location, event.name, event.sentences)
                sentenceBuilder.finishNested()
                currentState
            }
            on<NestedWithArguments> { currentState, event ->
                sentenceBuilder.beginNested(event.location, event.name, event.sentences)
                InNestedWithArguments(currentState, event.location, event.name, event.sentences)
            }
            on<CharacterLiteral> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<NullLiteral> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<BooleanLiteral> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<StringLiteral> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<MultilineString> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<NumberLiteral> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<Operator> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            ignoreAll(
                any<Terminal>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>(),
                any<EnterValueArguments>(),
                any<ExitValueArguments>()
            )
        }
        state<InNestedWithArgumentsParameter> {
            on<ExitValueArgument> { currentState, event ->
                currentState.parentState
            }
            registerAppendableEvents()

            ignoreAll(
                any<Terminal>(),
                any<EnterExpression>(),
                any<ExitExpression>(),
                any<EnterValueArgument>(),
                any<EnterValueArguments>(),
                any<ExitValueArguments>(),
            )
        }
        state<InNestedWithArguments> {
            on<EnterValueArguments> { currentState, event ->
                InNestedWithArgumentsParameter(currentState)
            }
            on<EnterValueArgument> { currentState, event ->
                InNestedWithArgumentsParameter(currentState)
            }
            on<ExitValueArguments> { currentState, event ->
                sentenceBuilder.finishNested(currentState.parameterEvents)
                currentState.parentState
            }

            registerAppendableEvents()

            ignoreAll(any<Terminal>(), any<EnterExpression>(), any<ExitExpression>())
        }
        state<InTypeArguments> {
            on<ExitTypeArguments> { currentState, event ->
                currentState.parentState
            }
            ignoreAll(any<Terminal>(), any<Identifier>())
        }
        state<InMethodInvocation> {
            on<ExitMethodInvocation> { currentState, _ ->
                currentState.parentState.also {
                    if (it is InMethodInvocation && it.didBegin) finishSentence()
                }
            }
            on<ChainedCallExpression> { currentState, event ->
                sentenceBuilder.append(event)

                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<Method> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<EnterExpression> { currentState, event ->
                InExpression(currentState)
            }
            ignoreAll(any<Terminal>(), any<ExitValueArguments>())
        }
    }

    private fun <T : WithAppendable> StateMachineBuilder<State, Event>.TransitionsBuilder<T>.registerAppendableEvents() {
        on<NullLiteral> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<FixturesExpression> { currentState, event ->
            currentState.append(event)
            InFixturesExpression(currentState)
        }
        on<BooleanLiteral> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<NumberLiteral> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<StringLiteral> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<Identifier> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<Operator> { currentState, event ->
            currentState.apply { append(event) }
        }
        on<ChainedCallExpression> { currentState, event ->
            currentState.append(event)
            InChainedCallExpression(currentState)
        }
    }

    fun apply(event: Event) {
        stateMachine.apply(event)
    }
}