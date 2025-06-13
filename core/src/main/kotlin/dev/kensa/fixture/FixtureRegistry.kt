package dev.kensa.fixture

import dev.kensa.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/**
 * A registry for fixtures.
 * Stores fixtures by their variable names and allows looking them up.
 */
object FixtureRegistry {
    private val lock = Any()
    private val fixturesByName = mutableMapOf<String, Fixture<*>>()
    private val fixturesByKey = mutableMapOf<String, Fixture<*>>()

    internal fun clearFixtures() {
        synchronized(this) {
            fixturesByName.clear()
            fixturesByKey.clear()
        }
    }

    /**
     * Looks up a fixture by its variable name.
     *
     * @param name The variable name of the fixture
     * @return The fixture, or null if not found
     * @throws IllegalArgumentException if no fixture with the given variable name is registered
     */
    @Suppress("UNCHECKED_CAST")
    internal fun <T> lookupFixture(name: String): Fixture<T> = fixturesByName[name] as? Fixture<T> ?: error("No fixture with name [$name] registered")

    @JvmStatic
    fun registerFixtures(vararg containers: FixtureContainer) = containers.forEach { register(it.javaClass) }

    @JvmStatic
    @SafeVarargs
    fun registerFixtures(vararg containers: Class<out FixtureContainer>) = containers.forEach { register(it) }

    internal fun register(containerClass: Class<out FixtureContainer>) {
        synchronized(lock) {
            if (containerClass.isKotlinClass) {
                val kClass = containerClass.kotlin
                if (kClass.isKotlinObject) {
                    kClass.registerFixtureProperties()
                }
            } else {
                containerClass.registerFixtureFields()
            }
        }
    }

    private fun KClass<out FixtureContainer>.registerFixtureProperties() {
        memberProperties.forEach { property ->
            if (property.isPublic() && property.hasType<Fixture<*>>()) {
                val newFixture = property.getter.call(objectInstance) as Fixture<*>
                newFixture.register(property.name)
            }
        }
    }

    private fun Class<out FixtureContainer>.registerFixtureFields() {
        for (field in this.declaredFields) {
            if (field.isPublicStatic() && field.hasType<Fixture<*>>()) {
                val newFixture = field.valueOfJavaStaticField<Fixture<*>>() ?: error("Fixture field [${field.declaringClass.simpleName}.${field.name}] contains a null value")
                newFixture.register(field.name)

            }
        }
    }

    private fun Fixture<*>.register(name: String) {
        // Ensure this name is not already registered for a different key
        fixturesByName[name]?.also { existingFixture ->
            if (existingFixture === this || existingFixture.key == this.key) {
                return
            }
            throw IllegalStateException("Duplicate fixture (field/property) name: ${name}. A fixture with this name is already registered with key: ${existingFixture.key}")
        }

        // Ensure a fixture with the same key is not already registered
        fixturesByKey[this.key]?.also { existingFixture ->
            if (existingFixture === this) {
                // Register the new fixture by name
                fixturesByName[name] = this
                return
            }
            throw IllegalStateException("Duplicate fixture key: ${this.key}. A fixture with this key is already registered with name: ${fixturesByName.entries.find { it.value.key == key }?.key}")
        }

        fixturesByName[name] = this
        fixturesByKey[key] = this
    }
}
