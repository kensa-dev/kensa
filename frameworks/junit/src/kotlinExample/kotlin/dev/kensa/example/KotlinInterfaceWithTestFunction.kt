package dev.kensa.example

import dev.kensa.GivensBuilder
import dev.kensa.junit.KensaTest
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

interface KotlinInterfaceWithTestFunction : KensaTest {

    @Test
    fun interfaceTest() {
        given(somePrerequisites())
    }

    private fun somePrerequisites(): GivensBuilder = GivensBuilder { givens: Givens -> givens.put("foo", "bar") }
}