package dev.kensa.parse.state

import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.parse.state.StateMachineTest.Event.*
import dev.kensa.parse.state.StateMachineTest.State.*
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StateMachineTest {

    sealed class State {
        data object State1 : State()
        data object State2 : State()
        data object State3 : State()
    }

    sealed class Event {
        data object Event1 : Event()
        data object Event2 : Event()
        data object Event3 : Event()
        data object Event4 : Event()
    }

    private lateinit var lastState: State
    private lateinit var lastEvent: Event
    private lateinit var stateMachine: StateMachine<State, Event>

    @BeforeEach
    internal fun setUp() {
        stateMachine = aStateMachine {
            initialState = State1

            // State style 1
            state(State1) {
                // On style 1
                on(Event2) { state, event ->
                    lastState = state
                    lastEvent = event
                    State2
                }

                // On style 2
                on<Event3> { state, event ->
                    lastState = state
                    lastEvent = event
                    State3
                }
            }

            // State style 2
            state<State2> {
                ignore(Event1)

                // On style 4
                on<Event3>(transitionTo(State3))

                on<Event4> { state, event ->
                    lastState = state
                    lastEvent = event
                    State3
                }
            }

            state<State3> {
                ignore(Matcher.eq(Event1))
            }
        }
    }

    @Test
    internal fun `can transition states within legal definitions and capture events`() {
        stateMachine.state shouldBe State1

        stateMachine.apply(Event2)
        stateMachine.state shouldBe State2
        lastState shouldBe State1
        lastEvent shouldBe Event2

        stateMachine.apply(Event4)
        stateMachine.state shouldBe State3
        lastState shouldBe State2
        lastEvent shouldBe Event4
    }

    @Test
    internal fun `can ignore events from certain states`() {
        stateMachine.apply(Event2)
        stateMachine.state shouldBe State2

        stateMachine.apply(Event1)
        stateMachine.state shouldBe State2

        stateMachine.apply(Event3)
        stateMachine.state shouldBe State3

        stateMachine.apply(Event1)
        stateMachine.state shouldBe State3
    }

    @Test
    internal fun `throws on illegal transitions`() {
        shouldThrowExactly<IllegalStateException> { stateMachine.apply(Event4) }
    }
}