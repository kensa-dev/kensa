package dev.kensa.parse.state

import dev.kensa.parse.state.StateMachineBuilder.Companion.aStateMachine
import dev.kensa.parse.state.StateMachineTest.Event.*
import dev.kensa.parse.state.StateMachineTest.State.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StateMachineTest {

    sealed class State {
        object State1 : State()
        object State2 : State()
        object State3 : State()
    }

    sealed class Event {
        object Event1 : Event()
        object Event2 : Event()
        object Event3 : Event()
        object Event4 : Event()
    }

    private lateinit var lastState: State
    private lateinit var lastEvent: Event
    private lateinit var stateMachine: StateMachine<State, Event>

    @BeforeEach
    internal fun setUp() {
        stateMachine = aStateMachine<State, Event> {
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
        assertThat(stateMachine.state).isEqualTo(State1)

        stateMachine.transition(Event2)
        assertThat(stateMachine.state).isEqualTo(State2)
        assertThat(lastState).isEqualTo(State1)
        assertThat(lastEvent).isEqualTo(Event2)

        stateMachine.transition(Event4)
        assertThat(stateMachine.state).isEqualTo(State3)
        assertThat(lastState).isEqualTo(State2)
        assertThat(lastEvent).isEqualTo(Event4)
    }

    @Test
    internal fun `can ignore events from certain states`() {
        stateMachine.transition(Event2)
        assertThat(stateMachine.state).isEqualTo(State2)

        stateMachine.transition(Event1)
        assertThat(stateMachine.state).isEqualTo(State2)

        stateMachine.transition(Event3)
        assertThat(stateMachine.state).isEqualTo(State3)

        stateMachine.transition(Event1)
        assertThat(stateMachine.state).isEqualTo(State3)
    }

    @Test
    internal fun `throws on illegal transitions`() {
        assertThatThrownBy { stateMachine.transition(Event4) }.isInstanceOf(IllegalStateException::class.java)
    }
}