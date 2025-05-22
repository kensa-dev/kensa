package dev.kensa.fixture

/**
 * A type-safe key for a fixture.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 */
sealed interface Fixture<T> {
    val key: String

    fun createValue(fixtures: Fixtures): T
}

/**
 * A primary fixture key.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 * @property factory Factory function to create the fixture value
 */
class PrimaryFixture<T>(
    override val key: String,
    val factory: () -> T
) : Fixture<T> {
    override fun createValue(fixtures: Fixtures): T = factory()
}

/**
 * A secondary fixture key.
 *
 * @param T The type of the fixture value
 * @param P The type of the parent fixture value
 * @property key The string key used internally
 * @property parent The parent fixture key
 * @property factory Function to transform the parent fixture value into the child fixture value
 */
class SecondaryFixture<T, P>(
    override val key: String,
    val parent: Fixture<P>,
    val factory: (P) -> T
) : Fixture<T> {
    override fun createValue(fixtures: Fixtures): T = factory(fixtures[parent])
}

/**
 * Creates a parent fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param factory Factory function to create the fixture value
 * @return A new parent fixture key
 */
inline fun <reified T> fixture(key: String, noinline factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory)

/**
 * Creates a secondary fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param primaryFixture The parent fixture key
 * @param factory Function to transform the parent fixture value into the secondary fixture value
 * @return A new secondary fixture key
 */
inline fun <reified T, P> fixture(key: String, primaryFixture: PrimaryFixture<P>, noinline factory: (P) -> T): SecondaryFixture<T, P> = SecondaryFixture(key, primaryFixture, factory)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param factory Factory function to create the fixture value
 * @return A new parent fixture key
 */
@JvmName("createFixture")
fun <T> createFixture(key: String, factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param primaryFixture The parent fixture key
 * @param factory Function to transform the parent fixture value into the secondary fixture value
 * @return A new secondary fixture key
 */
@JvmName("createFixture")
fun <T, P> createFixture(key: String, primaryFixture: PrimaryFixture<P>, factory: (P) -> T): SecondaryFixture<T, P> = SecondaryFixture(key, primaryFixture, factory)
