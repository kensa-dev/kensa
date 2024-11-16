package dev.kensa.parse.state

class StateMachine<STATE : Any, EVENT : Any>(initialState: STATE, private val transitions: Map<Matcher<STATE>, Set<StateMachineBuilder<STATE, EVENT>.Transition<EVENT, STATE>>>) {

    var state: STATE = initialState

    fun transition(event: EVENT) {
        val value = transitions.entries.firstOrNull { entry ->
            entry.key.matches(state)
        }?.value
        state = value
                ?.firstOrNull { it.matcher.matches(event) }
                ?.transitionFunc?.invoke(state, event)
//                ?.also { println("Event $event applied :: State is now $it") }
                ?: error("No transition for state ${state::class} -> event ${event::class}")
    }
}

class Matcher<T> private constructor(private val clazz: Class<T>) {

    private val predicates = mutableListOf<(T) -> Boolean>({ clazz.isInstance(it) })

    fun where(predicate: T.() -> Boolean): Matcher<T> = apply {
        predicates.add {
            it.predicate()
        }
    }

    fun matches(value: T) = predicates.all { it(value) }

    companion object {
        fun <T> any(clazz: Class<T>): Matcher<T> = Matcher(clazz)

        inline fun <reified T> any(): Matcher<T> = any(T::class.java)

        inline fun <reified T> eq(value: T): Matcher<T> = any<T>().where { this == value }
    }
}

class StateMachineBuilder<STATE : Any, EVENT : Any> {

    var initialState: STATE? = null
    private val transitions: MutableMap<Matcher<STATE>, Set<Transition<EVENT, STATE>>> = LinkedHashMap()

    fun <S : STATE> state(matcher: Matcher<S>, init: TransitionsBuilder<S>.() -> Unit) {
        @Suppress("UNCHECKED_CAST")
        transitions[matcher as Matcher<STATE>] = TransitionsBuilder<S>().apply(init).build() as Set<Transition<EVENT, STATE>>
    }

    inline fun <reified S : STATE> state(noinline init: TransitionsBuilder<S>.() -> Unit) {
        state(Matcher.any(), init)
    }

    inline fun <reified S : STATE> state(state: S, noinline init: TransitionsBuilder<S>.() -> Unit) {
        state(Matcher.eq(state), init)
    }

    fun build() = StateMachine(requireNotNull(initialState, { "An initial state must be provided" }), transitions)

    companion object {
        fun <STATE : Any, EVENT : Any> aStateMachine(init: StateMachineBuilder<STATE, EVENT>.() -> Unit): StateMachine<STATE, EVENT> {
            return StateMachineBuilder<STATE, EVENT>().apply(init).build()
        }
    }

    inner class Transition<E : EVENT, S : STATE>(val matcher: Matcher<E>, val transitionFunc: (S, E) -> STATE)

    inner class TransitionsBuilder<S : STATE> {

        private val transitions = LinkedHashSet<Transition<EVENT, S>>()

        fun <E : EVENT> on(matcher: Matcher<E>, transition: (S, E) -> STATE) {
            @Suppress("UNCHECKED_CAST")
            transitions += Transition(matcher, transition) as Transition<EVENT, S>
        }

        inline fun <reified E : EVENT> on(noinline transition: (S, E) -> STATE) {
            on(Matcher.any(), transition)
        }

        inline fun <reified E : EVENT> on(event: E, noinline transition: (S, E) -> STATE) {
            on(Matcher.eq(event), transition)
        }

        inline fun <reified E : EVENT> on(event: E, newState: STATE) {
            on(event) { _, _ -> newState }
        }

        inline fun <reified E : EVENT> on(newState: STATE) {
            on(Matcher.any<E>()) { _, _ -> newState }
        }

        inline fun <reified E : EVENT> ignore(event: E) {
            on(event) { s, _ -> s }
        }

        inline fun <reified E : EVENT> ignoreAll(init: MutableList<Matcher<out E>>.() -> Unit) {
            ArrayList<Matcher<out E>>().apply(init).forEach { matcher ->
                ignore(matcher)
            }
        }

        inline fun <reified E : EVENT> ignore(matcher: Matcher<out E>) {
            on(matcher) { s, e ->
//                println("Ignoring $e at state $s")
                s
            }
        }

        inline fun <reified E : EVENT> transitionTo(state: STATE): (S, E) -> STATE = { _, _ -> state }

        fun build(): Set<Transition<EVENT, S>> = transitions
    }
}