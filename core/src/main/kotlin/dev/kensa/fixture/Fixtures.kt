package dev.kensa.fixture

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

    fun hasValue(fixture: Fixture<*>): Boolean = synchronized(lock) { keyToValue.containsKey(fixture.key) }

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

    /**
     * Sets the value of a primary fixture.
     * Secondary fixtures cannot be written to.
     * When a primary fixture is updated, all its dependent secondary fixtures are cleared.
     *
     * @param fixture The key of the fixture
     * @param value The new value for the fixture
     * @throws IllegalArgumentException if the fixture is a secondary fixture
     */
    operator fun <T> set(fixture: PrimaryFixture<T>, value: T) {
        synchronized(lock) {
            keyToFixture[fixture.key] = fixture
            keyToValue[fixture.key] = value

            clearDependentFixtures(fixture)
        }
    }

    private fun clearDependentFixtures(fixture: Fixture<*>) {
        keyToFixture.filterValues { it is SecondaryFixture<*, *> && it.parent == fixture }
            .map {
                clearDependentFixtures(it.value)
                it.key

            }
            .forEach { secondaryKey ->
                keyToValue.remove(secondaryKey)
            }
    }

    private fun <T> createFixtureValue(fixture: Fixture<T>): T = fixture.createValue(this)
}
