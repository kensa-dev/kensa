 package dev.kensa.fixture

/**
 * A type-safe key for a fixture.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 */
sealed interface Fixture<T> {
    val key: String
    val highlighted: Boolean

    fun createValue(fixtures: Fixtures): T
}

fun interface SecondaryFixtureFactory<R> {
    operator fun invoke(exchange: Fixtures): R
}

sealed interface Parents {
    class One<F>(val parent1: Fixture<F>) : Parents
    class Two<F, F2>(val parent1: Fixture<F>, val parent2: Fixture<F2>) : Parents
    class Three<F, F2, F3>(val parent1: Fixture<F>, val parent2: Fixture<F2>, val parent3: Fixture<F3>) : Parents
}

/**
 * A primary fixture.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 * @property factory Factory function to create the fixture value
 */
class PrimaryFixture<T>(
    override val key: String,
    val factory: () -> T,
    override val highlighted: Boolean = false
) : Fixture<T> {
    override fun createValue(fixtures: Fixtures): T = factory()
}

/**
 * A secondary fixture.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 * @property factory Function to transform the parent fixture values into the child fixture value
 * @property parents The parent fixtures
 */
class SecondaryFixture<T>(
    override val key: String,
    private val factory: SecondaryFixtureFactory<T>,
    private val parents: Parents,
    override val highlighted: Boolean = false
) : Fixture<T> {
    fun isDependentOf(fixture: Fixture<*>): Boolean =
        when (parents) {
            is Parents.One<*> -> parents.parent1 == fixture
            is Parents.Two<*, *> -> parents.parent1 == fixture || parents.parent2 == fixture
            is Parents.Three<*, *, *> -> parents.parent1 == fixture || parents.parent2 == fixture || parents.parent3 == fixture
        }

    fun parentKeys(): List<String> =
        when (parents) {
            is Parents.One<*> -> listOf(parents.parent1.key)
            is Parents.Two<*, *> -> listOf(parents.parent1.key, parents.parent2.key)
            is Parents.Three<*, *, *> -> listOf(parents.parent1.key, parents.parent2.key, parents.parent3.key)
        }

    override fun createValue(fixtures: Fixtures): T = factory(fixtures)
}

/**
 * A fixture whose value is derived per-invocation from a named parameterised-test argument.
 *
 * @param T The type of the fixture value
 * @property key The string key used internally
 * @property from The name of the test parameter the value is derived from
 * @property transform Function to transform the parameter value into the fixture value
 */
class ParameterFixture<T>(
    override val key: String,
    val from: String,
    private val transform: (Any?) -> T,
    override val highlighted: Boolean = false
) : Fixture<T> {
    internal fun deriveFrom(argument: Any?): T = transform(argument)

    override fun createValue(fixtures: Fixtures): T =
        error("Parameter fixture '$key' must be seeded from test parameter '$from' — it cannot be resolved without a parameterised invocation.")
}

/**
 * Creates a parameter-derived fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param from The name of the test parameter the value is derived from
 * @param transform Function to transform the parameter value into the fixture value
 * @return A new parameter fixture
 */
inline fun <reified T, P> parameterFixture(key: String, from: String, noinline transform: (P) -> T): ParameterFixture<T> =
    ParameterFixture(key, from, { @Suppress("UNCHECKED_CAST") transform(it as P) })

inline fun <reified T, P> parameterFixture(key: String, from: String, highlighted: Boolean = false, noinline transform: (P) -> T): ParameterFixture<T> =
    ParameterFixture(key, from, { @Suppress("UNCHECKED_CAST") transform(it as P) }, highlighted)

/**
 * Creates a parent fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param factory Factory function to create the fixture value
 * @return A new parent fixture
 */
inline fun <reified T> fixture(key: String, noinline factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory)

inline fun <reified T> fixture(key: String, highlighted: Boolean = false, noinline factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory, highlighted)

/**
 * The storage identity of a `@`[dev.kensa.Fixture] factory fixture: its annotation [key] combined with the
 * `toString()` of each identity argument. A distinct argument-set is a distinct, memoized fixture; the same
 * argument-set (by value) resolves to the same cached value. Order-significant per the argument list.
 */
internal fun compositeFixtureKey(key: String, identityArgs: List<Any?>): String =
    if (identityArgs.isEmpty()) key else "$key(${identityArgs.joinToString(", ")})"

/**
 * Creates a `@`[dev.kensa.Fixture] factory fixture whose identity is `(key, identityArgs)`. The Kensa compiler
 * plugin rewrites a no-name `fixture { }` inside a `@Fixture("key")` function to this function, injecting the
 * key and the factory's own value parameters as [identityArgs]. Distinct argument-sets are distinct, memoized
 * fixtures; the same argument-set resolves to the same cached value (a singleton per identity).
 *
 * Named distinctly from [fixture] to avoid overload ambiguity with the secondary-fixture overloads; users never
 * write this directly — they write the no-name `fixture { }` and rely on the plugin rewrite.
 */
fun <T> factoryFixture(key: String, vararg identityArgs: Any?, factory: () -> T): PrimaryFixture<T> =
    PrimaryFixture(compositeFixtureKey(key, identityArgs.toList()), factory)

/**
 * Creates a primary fixture whose key is taken from the enclosing `@`[dev.kensa.Fixture]`("key")` factory
 * function, so the key need not be repeated:
 *
 * ```
 * @Fixture("MyFixture")
 * fun myFixture(param: String) = fixture { if (param == "Meh") ... else ... }
 * ```
 *
 * The key and identity arguments are injected at compile time by the Kensa compiler plugin, which rewrites
 * this no-name call to the keyed [fixture] overload. Reaching this overload at runtime means the plugin was
 * not applied to the source set, so it fails loud rather than mis-rendering silently.
 */
fun <T> fixture(@Suppress("UNUSED_PARAMETER") factory: () -> T): PrimaryFixture<T> =
    error(
        "fixture { } without a key requires the Kensa compiler plugin on this source set — it rewrites the " +
            "enclosing @dev.kensa.Fixture(\"...\") factory's fixture { } to fixture(key, args) { }. Apply the " +
            "plugin, or use the keyed fixture(key) { } overload directly."
    )

/**
 * Creates a secondary fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param parentFixture The parent fixture
 * @param factory Function to transform the parent fixture value into the secondary fixture value
 * @return A new secondary fixture
 */
inline fun <reified T, P1> fixture(key: String, parentFixture: Fixture<P1>, noinline factory: (P1) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture]) }, Parents.One(parentFixture))

inline fun <reified T, P1> fixture(key: String, parentFixture: Fixture<P1>, highlighted: Boolean = false, noinline factory: (P1) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture]) }, Parents.One(parentFixture), highlighted)

/**
 * Creates a secondary fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param parentFixture1 The first parent fixture
 * @param parentFixture2 The second parent fixture
 * @param factory Function to transform the parent fixture values into the secondary fixture value
 * @return A new secondary fixture
 */
inline fun <reified T, P1, P2> fixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, noinline factory: (P1, P2) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2]) }, Parents.Two(parentFixture1, parentFixture2))

inline fun <reified T, P1, P2> fixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, highlighted: Boolean = false, noinline factory: (P1, P2) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2]) }, Parents.Two(parentFixture1, parentFixture2), highlighted)

/**
 * Creates a secondary fixture with a type-safe key.
 *
 * @param key The string key used internally
 * @param parentFixture1 The first parent fixture
 * @param parentFixture2 The second parent fixture
 * @param parentFixture3 The third parent fixture
 * @param factory Function to transform the parent fixture values into the secondary fixture value
 * @return A new secondary fixture
 */
inline fun <reified T, P1, P2, P3> fixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, parentFixture3: Fixture<P3>, noinline factory: (P1, P2, P3) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2], fixtures[parentFixture3]) }, Parents.Three(parentFixture1, parentFixture2, parentFixture3))

inline fun <reified T, P1, P2, P3> fixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, parentFixture3: Fixture<P3>, highlighted: Boolean = false, noinline factory: (P1, P2, P3) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2], fixtures[parentFixture3]) }, Parents.Three(parentFixture1, parentFixture2, parentFixture3), highlighted)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param factory Factory function to create the fixture value
 * @return A new parent fixture
 */
@JvmName("createFixture")
fun <T> createFixture(key: String, factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory)

@JvmName("createFixture")
fun <T> createFixture(key: String, highlighted: Boolean, factory: () -> T): PrimaryFixture<T> = PrimaryFixture(key, factory, highlighted)

/**
 * Non-inline version of the parameterFixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param from The name of the test parameter the value is derived from
 * @param transform Function to transform the parameter value into the fixture value
 * @return A new parameter fixture
 */
@JvmName("createParameterFixture")
fun <T, P> createParameterFixture(key: String, from: String, transform: (P) -> T): ParameterFixture<T> =
    ParameterFixture(key, from, { @Suppress("UNCHECKED_CAST") transform(it as P) })

@JvmName("createParameterFixture")
fun <T, P> createParameterFixture(key: String, from: String, highlighted: Boolean, transform: (P) -> T): ParameterFixture<T> =
    ParameterFixture(key, from, { @Suppress("UNCHECKED_CAST") transform(it as P) }, highlighted)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param parentFixture The parent fixture
 * @param factory Function to transform the parent fixture value into the secondary fixture value
 * @return A new secondary fixture
 */
@JvmName("createFixture")
fun <T, P1> createFixture(key: String, parentFixture: Fixture<P1>, factory: (P1) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture]) }, Parents.One(parentFixture))

@JvmName("createFixture")
fun <T, P1> createFixture(key: String, parentFixture: Fixture<P1>, highlighted: Boolean, factory: (P1) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture]) }, Parents.One(parentFixture), highlighted)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param parentFixture1 The first parent fixture
 * @param parentFixture2 The second parent fixture
 * @param factory Function to transform the parent fixture values into the secondary fixture value
 * @return A new secondary fixture
 */
@JvmName("createFixture")
fun <T, P1, P2> createFixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, factory: (P1, P2) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2]) }, Parents.Two(parentFixture1, parentFixture2))

@JvmName("createFixture")
fun <T, P1, P2> createFixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, highlighted: Boolean, factory: (P1, P2) -> T): SecondaryFixture<T> = SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2]) }, Parents.Two(parentFixture1, parentFixture2), highlighted)

/**
 * Non-inline version of the fixture function for Java interoperability.
 *
 * @param key The string key used internally
 * @param parentFixture1 The first parent fixture
 * @param parentFixture2 The second parent fixture
 * @param parentFixture3 The third parent fixture
 * @param factory Function to transform the parent fixture values into the secondary fixture value
 * @return A new secondary fixture
 */
@JvmName("createFixture")
fun <T, P1, P2, P3> createFixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, parentFixture3: Fixture<P3>, factory: (P1, P2, P3) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2], fixtures[parentFixture3]) }, Parents.Three(parentFixture1, parentFixture2, parentFixture3))

@JvmName("createFixture")
fun <T, P1, P2, P3> createFixture(key: String, parentFixture1: Fixture<P1>, parentFixture2: Fixture<P2>, parentFixture3: Fixture<P3>, highlighted: Boolean, factory: (P1, P2, P3) -> T): SecondaryFixture<T> =
    SecondaryFixture(key, { fixtures -> factory(fixtures[parentFixture1], fixtures[parentFixture2], fixtures[parentFixture3]) }, Parents.Three(parentFixture1, parentFixture2, parentFixture3), highlighted)
