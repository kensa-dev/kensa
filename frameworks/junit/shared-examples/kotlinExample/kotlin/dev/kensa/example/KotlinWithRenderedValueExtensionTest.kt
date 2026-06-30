package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext
import dev.kensa.Sources
import org.junit.jupiter.api.Test

@Sources(RenderHelpers::class)
class KotlinWithRenderedValueExtensionTest : KotlinExampleTest() {

    @Test
    fun rendersChainedEnumProperty() = with(RenderHelpers) {
        given(somethingWith(productFor("f").stringValue))
    }

    @Test
    fun rendersChainedDataClassProperty() = with(RenderHelpers) {
        given(somethingWith(wrappedFor("baz").stringValue))
    }

    @Test
    fun rendersBareRenderedValueCall() = with(RenderHelpers) {
        given(somethingWith(productFor("f")))
    }

    private fun somethingWith(value: Any?) = Action<GivensContext> {}
}
