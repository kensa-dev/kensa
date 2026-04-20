package dev.kensa

import dev.kensa.RenderedHintStrategy.NoHint
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

internal class RenderingDirectivesTest {

    private fun directive(param: String) = RenderingDirective(UseIdentifierName, "", NoHint, param)

    private sealed interface Animal
    private abstract class Mammal : Animal
    private class Dog : Mammal()
    private class Fish : Animal

    @Test
    fun `returns exact match`() {
        val dogDirective = directive("dog")
        val directives = RenderingDirectives(mapOf(Dog::class to dogDirective))

        directives[Dog::class] shouldBe dogDirective
    }

    @Test
    fun `returns directive declared on supertype for subtype lookup`() {
        val mammalDirective = directive("mammal")
        val directives = RenderingDirectives(mapOf(Mammal::class to mammalDirective))

        directives[Dog::class] shouldBe mammalDirective
    }

    @Test
    fun `returns directive declared on implemented interface`() {
        val animalDirective = directive("animal")
        val directives = RenderingDirectives(mapOf(Animal::class to animalDirective))

        directives[Dog::class] shouldBe animalDirective
        directives[Fish::class] shouldBe animalDirective
    }

    @Test
    fun `most specific declared type wins`() {
        val animalDirective = directive("animal")
        val mammalDirective = directive("mammal")
        val dogDirective = directive("dog")
        val directives = RenderingDirectives(
            mapOf(
                Animal::class to animalDirective,
                Mammal::class to mammalDirective,
                Dog::class to dogDirective
            )
        )

        directives[Dog::class] shouldBe dogDirective
        directives[Mammal::class] shouldBe mammalDirective
        directives[Fish::class] shouldBe animalDirective
    }

    @Test
    fun `returns null when no directive matches`() {
        val directives = RenderingDirectives(mapOf(Mammal::class to directive("mammal")))

        directives[Fish::class].shouldBeNull()
    }

    @Test
    fun `returns null for null lookup`() {
        val directives = RenderingDirectives(mapOf(Animal::class to directive("animal")))

        directives[null as KClass<*>?].shouldBeNull()
    }

    @Test
    fun `empty directives returns null for any lookup`() {
        RenderingDirectives()[Dog::class].shouldBeNull()
    }

    @Test
    fun `appliesTo mirrors get`() {
        val directives = RenderingDirectives(mapOf(Mammal::class to directive("mammal")))

        directives.appliesTo(Dog::class).shouldBeTrue()
        directives.appliesTo(Fish::class).shouldBeFalse()
        directives.appliesTo(null).shouldBeFalse()
    }
}
