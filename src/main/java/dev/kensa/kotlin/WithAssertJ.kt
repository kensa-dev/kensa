package dev.kensa.kotlin

import dev.kensa.StateExtractor
import dev.kensa.context.AssertJThen
import dev.kensa.context.TestContextHolder.testContext
import org.assertj.core.api.*
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.time.*
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface WithAssertJ {

    private fun <T, A> then(extractor: StateExtractor<T>, assertProvider: (T?) -> A): A =
        AssertJThen.then(testContext(), extractor, assertProvider)

    fun <T, A> thenEventually(duration: Duration = 10.seconds, extractor: StateExtractor<T>, assertProvider: (T?) -> A, block: A.() -> Unit) =
        AssertJThen.thenEventually(duration, testContext(), extractor) { actual ->
            assertProvider(actual).apply(block)
        }

    fun <T, A> then(extractor: StateExtractor<T>, assertProvider: (T?) -> A, block: A.() -> Unit) =
        AssertJThen.then(testContext(), extractor) { actual ->
            assertProvider(actual).apply(block)
        }

    fun <T> then(extractor: StateExtractor<Optional<T>>): OptionalAssert<T> = then(extractor) { actual: Optional<T>? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<OptionalDouble>): OptionalDoubleAssert = then(extractor) { actual: OptionalDouble? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<OptionalInt>): OptionalIntAssert = then(extractor) { actual: OptionalInt? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<OptionalLong>): OptionalLongAssert = then(extractor) { actual: OptionalLong? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<BigDecimal?>): BigDecimalAssert = then(extractor, ::BigDecimalAssert)

    fun then(extractor: StateExtractor<BigInteger?>): BigIntegerAssert = then(extractor, ::BigIntegerAssert)

    fun then(extractor: StateExtractor<URI?>): UriAssert = then(extractor, ::UriAssert)

    fun then(extractor: StateExtractor<URL?>): UrlAssert = then(extractor, ::UrlAssert)

    fun then(extractor: StateExtractor<Boolean?>): BooleanAssert = then(extractor, ::BooleanAssert)

    fun then(extractor: StateExtractor<String?>): StringAssert = then(extractor, ::StringAssert)

    fun then(extractor: StateExtractor<Byte?>): ByteAssert = then(extractor, ::ByteAssert)

    fun then(extractor: StateExtractor<ByteArray?>): ByteArrayAssert = then(extractor, ::ByteArrayAssert)

    fun then(extractor: StateExtractor<Char?>): CharacterAssert = then(extractor, ::CharacterAssert)

    fun then(extractor: StateExtractor<CharArray?>): CharArrayAssert = then(extractor, ::CharArrayAssert)

    fun then(extractor: StateExtractor<Class<*>?>): ClassAssert = then(extractor, ::ClassAssert)

    fun then(extractor: StateExtractor<Double?>): DoubleAssert = then(extractor) { actual: Double? -> DoubleAssert(actual) }

    fun then(extractor: StateExtractor<DoubleArray?>): DoubleArrayAssert = then(extractor, ::DoubleArrayAssert)

    fun then(extractor: StateExtractor<File?>): FileAssert = then(extractor, ::FileAssert)

    fun then(extractor: StateExtractor<Float?>): FloatAssert = then(extractor) { actual: Float? -> FloatAssert(actual) }

    fun then(extractor: StateExtractor<FloatArray?>): FloatArrayAssert = then(extractor, ::FloatArrayAssert)

    fun then(extractor: StateExtractor<Int?>): IntegerAssert = then(extractor, ::IntegerAssert)

    fun then(extractor: StateExtractor<IntArray?>): IntArrayAssert = then(extractor, ::IntArrayAssert)

    fun then(extractor: StateExtractor<Long?>): LongAssert = then(extractor, ::LongAssert)

    fun then(extractor: StateExtractor<LongArray?>): LongArrayAssert = then(extractor, ::LongArrayAssert)

    fun then(extractor: StateExtractor<Any?>): ObjectAssert<*> = then(extractor) { actual: Any? -> ObjectAssert(actual) }

    fun then(extractor: StateExtractor<Array<Any>?>): ObjectArrayAssert<*> = then(extractor) { actual: Array<Any>? -> ObjectArrayAssert(actual) }

    fun then(extractor: StateExtractor<Short?>): ShortAssert = then(extractor, ::ShortAssert)

    fun then(extractor: StateExtractor<ShortArray?>): ShortArrayAssert = then(extractor, ::ShortArrayAssert)

    fun then(extractor: StateExtractor<ZonedDateTime?>): AbstractZonedDateTimeAssert<*> = then(extractor) { actual: ZonedDateTime? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<LocalDateTime?>): AbstractLocalDateTimeAssert<*> = then(extractor) { actual: LocalDateTime? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<OffsetDateTime?>): AbstractOffsetDateTimeAssert<*> = then(extractor) { actual: OffsetDateTime? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<LocalTime?>): AbstractLocalTimeAssert<*> = then(extractor) { actual: LocalTime? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<LocalDate?>): AbstractLocalDateAssert<*> = then(extractor) { actual: LocalDate? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<Instant?>): AbstractInstantAssert<*> = then(extractor) { actual: Instant? -> AssertionsForClassTypes.assertThat(actual) }

    fun then(extractor: StateExtractor<Throwable?>): ThrowableAssert<*> = then(extractor, ::ThrowableAssert)

    fun <T> and(extractor: StateExtractor<Optional<T>>): OptionalAssert<T> = then(extractor)

    fun and(extractor: StateExtractor<OptionalDouble>): OptionalDoubleAssert = then(extractor)

    fun and(extractor: StateExtractor<OptionalInt>): OptionalIntAssert = then(extractor)

    fun and(extractor: StateExtractor<OptionalLong>): OptionalLongAssert = then(extractor)

    fun and(extractor: StateExtractor<BigDecimal?>): BigDecimalAssert = then(extractor)

    fun and(extractor: StateExtractor<BigInteger?>): BigIntegerAssert = then(extractor)

    fun and(extractor: StateExtractor<URI?>): UriAssert = then(extractor)

    fun and(extractor: StateExtractor<URL?>): UrlAssert = then(extractor)

    fun and(extractor: StateExtractor<Boolean?>): BooleanAssert = then(extractor)

    fun and(extractor: StateExtractor<String?>): StringAssert = then(extractor)

    fun and(extractor: StateExtractor<Byte?>): ByteAssert = then(extractor)

    fun and(extractor: StateExtractor<ByteArray?>): ByteArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<Char?>): CharacterAssert = then(extractor)

    fun and(extractor: StateExtractor<CharArray?>): CharArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<Class<*>?>): ClassAssert = then(extractor)

    fun and(extractor: StateExtractor<Double?>): DoubleAssert = then(extractor)

    fun and(extractor: StateExtractor<DoubleArray?>): DoubleArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<File?>): FileAssert = then(extractor)

    fun and(extractor: StateExtractor<Float?>): FloatAssert = then(extractor)

    fun and(extractor: StateExtractor<FloatArray?>): FloatArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<Int?>): IntegerAssert = then(extractor)

    fun and(extractor: StateExtractor<IntArray?>): IntArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<Long?>): LongAssert = then(extractor)

    fun and(extractor: StateExtractor<LongArray?>): LongArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<Any?>): ObjectAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<Array<Any>?>): ObjectArrayAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<Short?>): ShortAssert = then(extractor)

    fun and(extractor: StateExtractor<ShortArray?>): ShortArrayAssert = then(extractor)

    fun and(extractor: StateExtractor<ZonedDateTime?>): AbstractZonedDateTimeAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<LocalDateTime?>): AbstractLocalDateTimeAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<OffsetDateTime?>): AbstractOffsetDateTimeAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<LocalTime?>): AbstractLocalTimeAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<LocalDate?>): AbstractLocalDateAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<Instant?>): AbstractInstantAssert<*> = then(extractor)

    fun and(extractor: StateExtractor<Throwable?>): ThrowableAssert<*> = then(extractor)

}