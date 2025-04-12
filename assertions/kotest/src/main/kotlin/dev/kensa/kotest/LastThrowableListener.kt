package dev.kensa.kotest

import io.kotest.assertions.nondeterministic.EventuallyListener

internal class LastThrowableListener : EventuallyListener {
    var lastThrowable: Throwable? = null
        private set

    override suspend fun invoke(iteration: Int, throwable: Throwable) {
        lastThrowable = throwable
    }
}
