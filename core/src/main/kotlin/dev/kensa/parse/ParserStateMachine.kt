package dev.kensa.parse

import dev.kensa.parse.Event.*
import dev.kensa.parse.LocatedEvent.*
import dev.kensa.parse.LocatedEvent.Literal.*
import dev.kensa.parse.LocatedEvent.PathExpression.*
import dev.kensa.parse.State.*
import dev.kensa.parse.State.WithAppendable.InNestedWithArguments
import dev.kensa.parse.State.WithAppendable.InNestedWithArgumentsParameter
import dev.kensa.parse.state.Matcher.Companion.any
import dev.kensa.parse.state.StateMachine
import dev.kensa.parse.state.StateMachineBuilder
import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateSentence

class ParserStateMachine(private val createSentenceBuilder: (Boolean, Location, Location) -> SentenceBuilder) {

    private val _sentences: MutableList<TemplateSentence> = ArrayList()
    val sentences: List<TemplateSentence>
        get() = _sentences

    private lateinit var sentenceBuilder: SentenceBuilder
    private var lastSentenceEndLocation: Location? = null

    private fun beginSentence(isNoteSentence : Boolean, location: Location) {
        sentenceBuilder = createSentenceBuilder(isNoteSentence, location, lastSentenceEndLocation ?: location)
    }

    private fun finishSentence() {
        _sentences += sentenceBuilder.build()
        lastSentenceEndLocation = sentenceBuilder.lastLocation
    }

    private fun beginOrContinueNoteSentence(location: Location) {
        if (!this::sentenceBuilder.isInitialized || !sentenceBuilder.isNoteBlock) {
            beginSentence(true, location)
        }
    }

    private fun finishNoteSentence() {
        if (this::sentenceBuilder.isInitialized && sentenceBuilder.isNoteBlock) {
            finishSentence()
        }
    }

    internal val stateMachine: StateMachine<State, Event> = aStateMachine {

        initialState = Start

        state<Start> {
            on<EnterMethod> { _, _ ->
                InMethod
            }
        }
        state<InMethod> {
            on<ExitMethod>(transitionTo(End))
            on<EnterBlock> { currentState, _ ->
                TestBlock(currentState)
            }
            on<EnterExpression> { currentState, _ ->
                ExpressionFn(currentState)
            }
            ignoreAll(any<Operator>(), any<Terminal>())
        }
        state<ExpressionFn> {
            on<EnterExpression> { currentState, _ ->
                ExpressionFn(currentState)
            }
            on<ChainedCallExpression> { currentState, _ ->
                ExpressionFn(currentState)
            }
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<EnterStatement> { currentState, event ->
                beginSentence(false, event.location)
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
            on<ExitBlock> { currentState, _ -> currentState.parentState }
            on<Note> { currentState, event ->
                beginOrContinueNoteSentence(event.location)
                sentenceBuilder.append(event)
                currentState
            }
            on<EnterStatement> { currentState, event ->
                finishNoteSentence()
                beginSentence(false, event.location)
                InStatement(currentState)
            }

            ignoreAll(any<Terminal>())
        }
        state<InStatement> {
            on<ExitStatement> { currentState, _ ->
                if (currentState.didBegin) finishSentence()
                currentState.parentState
            }
            on<Note> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<RenderedValue> { currentState, event ->
                sentenceBuilder.append(event.location, event)
                InRenderedValueExpression(currentState)
            }
            on<EnterExpression> { currentState, _ ->
                InExpression(currentState)
            }
            on<EnterMethodInvocation> { currentState, _ ->
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
            on<OutputsByNameExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByNameValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<OutputsByKeyExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByKeyValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                sentenceBuilder.append(event)

                InChainedCallExpression(currentState)
            }
            ignoreAll(any<Terminal>())
        }
        state<InFixturesExpression> {
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<FixturesExpression> { currentState, _ ->
                InFixturesExpression(currentState)
            }
            on<EnterExpression> { currentState, _ ->
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
        state<InOutputsExpression> {
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<OutputsByNameExpression> { currentState, _ ->
                InOutputsExpression(currentState)
            }
            on<OutputsByKeyExpression> { currentState, _ ->
                InOutputsExpression(currentState)
            }
            on<EnterExpression> { currentState, _ ->
                InOutputsExpression(currentState)
            }
            ignoreAll(any<EnterValueArguments>(),
                any<ExitValueArguments>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>(),
                any<Terminal>(),
                any<Identifier>(),
                any<Literal>(),
            )
        }
        state<InChainedCallExpression> {
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<ChainedCallExpression> { currentState, _ ->
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
            on<Note> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<ExitLambda> { currentState, _ ->
                currentState.parentState
            }
            on<EnterStatement> { currentState, _ ->
                InStatement(currentState, didBegin = false)
            }
            ignoreAll(any<Terminal>())
        }
        state<InRenderedValueExpression> {
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            onAny(
                RenderedValue::class,
                EnterExpression::class,
                ChainedCallExpression::class,
                FixturesExpression::class,
                OutputsByKeyExpression::class,
                OutputsByNameExpression::class
            ) { currentState, _ -> InRenderedValueExpression(currentState) }
            ignoreAll(
                any<Method>(),
                any<Terminal>(),
                any<Literal>(),
                any<EnterValueArguments>(),
                any<EnterValueArgument>(),
                any<ExitValueArgument>(),
                any<ExitValueArguments>(),
                any<ChainedCallExpression>(),
                any<Field>(),
                any<Parameter>(),
                any<Identifier>(),
            )
        }
        state<InExpression> {
            on<Note> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<EnterLambda> { currentState, _ ->
                InLambda(currentState)
            }
            on<EnterStatement> { currentState, _ ->
                InStatement(currentState)
            }
            on<EnterExpression> { currentState, _ ->
                InExpression(currentState)
            }
            on<ExitExpression> { currentState, _ ->
                currentState.parentState
            }
            on<RenderedValue> { currentState, event ->
                sentenceBuilder.append(event.location, event)
                InRenderedValueExpression(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                sentenceBuilder.append(event)
                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<OutputsByNameExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByNameValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<OutputsByKeyExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByKeyValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<Identifier> { currentState, event ->
                sentenceBuilder.append(event)
                currentState
            }
            on<EnterTypeArguments> { currentState, _ ->
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
            on<ExitValueArgument> { currentState, _ ->
                currentState.parentState
            }
            on<EnterValueArguments> { currentState, _ ->
                InNestedWithArgumentsParameter(currentState)
            }
            on<ExitValueArguments> { currentState, _ ->
                currentState.parentState
            }

            registerAppendableEvents()

            ignoreAll(
                any<Terminal>(),
                any<EnterExpression>(),
                any<ExitExpression>(),
                any<EnterValueArgument>(),
            )
        }
        state<InNestedWithArguments> {
            on<EnterValueArguments> { currentState, _ ->
                InNestedWithArgumentsParameter(currentState)
            }
            on<EnterValueArgument> { currentState, _ ->
                InNestedWithArgumentsParameter(currentState)
            }
            on<ExitValueArguments> { currentState, _ ->
                sentenceBuilder.finishNested(currentState.parameterEvents)
                currentState.parentState
            }

            registerAppendableEvents()

            ignoreAll(any<Terminal>(), any<EnterExpression>(), any<ExitExpression>(), any<ExitValueArgument>())
        }
        state<InTypeArguments> {
            on<ExitTypeArguments> { currentState, _ ->
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
            on<RenderedValue> { currentState, event ->
                sentenceBuilder.append(event.location, event)
                InRenderedValueExpression(currentState)
            }
            on<ChainedCallExpression> { currentState, event ->
                sentenceBuilder.append(event)

                InChainedCallExpression(currentState)
            }
            on<FixturesExpression> { currentState, event ->
                sentenceBuilder.appendFixturesValue(event.location, event.name, event.path)
                InFixturesExpression(currentState)
            }
            on<OutputsByNameExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByNameValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<OutputsByKeyExpression> { currentState, event ->
                sentenceBuilder.appendOutputsByKeyValue(event.location, event.name, event.path)
                InOutputsExpression(currentState)
            }
            on<Identifier> { currentState, event ->
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
            on<EnterExpression> { currentState, _ ->
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
        on<OutputsByNameExpression> { currentState, event ->
            currentState.append(event)
            InOutputsExpression(currentState)
        }
        on<OutputsByKeyExpression> { currentState, event ->
            sentenceBuilder.appendOutputsByKeyValue(event.location, event.name, event.path)
            InOutputsExpression(currentState)
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