package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

interface KotlinInterfaceWithTestFunction : KensaTest {

    @Test
    fun interfaceTest() {
        given(somePrerequisites())
    }

    private fun somePrerequisites() = Action<GivensContext> { }
}