package dev.kensa.kotest

import io.kotest.assertions.nondeterministic.EventuallyListener

internal class LastThrowableListener : EventuallyListener {
    lateinit var lastThrowable: Throwable
        private set

    override suspend fun invoke(iteration: Int, throwable: Throwable) {
        lastThrowable = throwable
    }
}
