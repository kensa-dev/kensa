package dev.kensa.fixture

import dev.kensa.util.NamedValue

/**
 * A type-safe map for managing test fixtures.
 * Fixtures are created lazily when first accessed.
 * Primary fixtures can be written to, while secondary fixtures are read-only.
 * This implementation is thread-safe.
 */
class Fixtures {
    private val lock = Any()
    private val keyToValue = mutableMapOf<String, Any?>()
    private val keyToFixture = mutableMapOf<String, Fixture<*>>()

    fun values(): Set<NamedValue> = keyToValue.entries.map { NamedValue(it.key, it.value) }.toSet()

    /**
     * Gets the value of a fixture by key.
     * The fixture is created lazily when first accessed.
     *
     * @param fixture The key of the fixture
     * @return The fixture value
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(fixture: Fixture<T>): T =
        synchronized(lock) {
            keyToValue.getOrPut(fixture.key) {
                keyToFixture[fixture.key] = fixture

                createFixtureValue(fixture)
            } as T
        }

    private fun <T> createFixtureValue(fixture: Fixture<T>): T = fixture.createValue(this)
}
