package dev.kensa.assertj;

import dev.kensa.StateCollector;
import org.assertj.core.api.*;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import static dev.kensa.context.TestContextHolder.testContext;
import static java.time.temporal.ChronoUnit.SECONDS;

public interface WithAssertJ {

    default <T, A> A then(StateCollector<T> collector, Function<T, A> assertProvider) {
        return AssertJThen.then(testContext(), collector, assertProvider::apply);
    }

    default <T, A> A thenEventually(StateCollector<T> collector, Function<T, A> assertProvider) {
        return thenEventually(10L, SECONDS, collector, assertProvider);
    }

    default <T, A> A thenEventually(Long timeout, ChronoUnit timeUnit, StateCollector<T> collector, Function<T, A> assertProvider) {
        return AssertJThen.thenEventually(timeout, timeUnit, testContext(), collector, assertProvider::apply);
    }

    default <T> OptionalAssert<T> then(OptionalStateCollector<T> collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default OptionalDoubleAssert then(OptionalDoubleStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default OptionalIntAssert then(OptionalIntStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default OptionalLongAssert then(OptionalLongStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default BigDecimalAssert then(BigDecimalStateCollector collector) {
        return then(collector, BigDecimalAssert::new);
    }

    default BigIntegerAssert then(BigIntegerStateCollector collector) {
        return then(collector, BigIntegerAssert::new);
    }

    default UriAssert then(UriStateCollector collector) {
        return then(collector, UriAssert::new);
    }

    default UrlAssert then(UrlStateCollector collector) {
        return then(collector, UrlAssert::new);
    }

    default BooleanAssert then(BooleanStateCollector collector) {
        return then(collector, BooleanAssert::new);
    }

    default StringAssert then(StringStateCollector collector) {
        return then(collector, StringAssert::new);
    }

    default ByteAssert then(ByteStateCollector collector) {
        return then(collector, ByteAssert::new);
    }

    default ByteArrayAssert then(ByteArrayStateCollector collector) {
        return then(collector, ByteArrayAssert::new);
    }

    default CharacterAssert then(CharacterStateCollector collector) {
        return then(collector, CharacterAssert::new);
    }

    default CharArrayAssert then(CharArrayStateCollector collector) {
        return then(collector, CharArrayAssert::new);
    }

    default ClassAssert then(ClassStateCollector collector) {
        return then(collector, ClassAssert::new);
    }

    default DoubleAssert then(DoubleStateCollector collector) {
        return then(collector, DoubleAssert::new);
    }

    default DoubleArrayAssert then(DoubleArrayStateCollector collector) {
        return then(collector, DoubleArrayAssert::new);
    }

    default FileAssert then(FileStateCollector collector) {
        return then(collector, FileAssert::new);
    }

    default FloatAssert then(FloatStateCollector collector) {
        return then(collector, FloatAssert::new);
    }

    default FloatArrayAssert then(FloatArrayStateCollector collector) {
        return then(collector, FloatArrayAssert::new);
    }

    default IntegerAssert then(IntegerStateCollector collector) {
        return then(collector, IntegerAssert::new);
    }

    default IntArrayAssert then(IntArrayStateCollector collector) {
        return then(collector, IntArrayAssert::new);
    }

    default LongAssert then(LongStateCollector collector) {
        return then(collector, LongAssert::new);
    }

    default LongArrayAssert then(LongArrayStateCollector collector) {
        return then(collector, LongArrayAssert::new);
    }

    default <T> ObjectAssert<T> then(StateCollector<T> collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default <T> ObjectArrayAssert<T> then(ObjectArrayStateCollector<T> collector) {
        return then(collector, ObjectArrayAssert::new);
    }

    default ShortAssert then(ShortStateCollector collector) {
        return then(collector, ShortAssert::new);
    }

    default ShortArrayAssert then(ShortArrayStateCollector collector) {
        return then(collector, ShortArrayAssert::new);
    }

    default AbstractZonedDateTimeAssert<?> then(ZonedDateTimeStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateTimeAssert<?> then(LocalDateTimeStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default AbstractOffsetDateTimeAssert<?> then(OffsetDateTimeStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalTimeAssert<?> then(LocalTimeStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateAssert<?> then(LocalDateStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default AbstractInstantAssert<?> then(InstantStateCollector collector) {
        return then(collector, AssertionsForClassTypes::assertThat);
    }

    default ThrowableAssert then(ThrowableStateCollector collector) {
        return then(collector, ThrowableAssert::new);
    }

    default <T> OptionalAssert<T> and(OptionalStateCollector<T> collector) {
        return then(collector);
    }

    default OptionalDoubleAssert and(OptionalDoubleStateCollector collector) {
        return then(collector);
    }

    default OptionalIntAssert and(OptionalIntStateCollector collector) {
        return then(collector);
    }

    default OptionalLongAssert and(OptionalLongStateCollector collector) {
        return then(collector);
    }

    default BigDecimalAssert and(BigDecimalStateCollector collector) {
        return then(collector);
    }

    default BigIntegerAssert and(BigIntegerStateCollector collector) {
        return then(collector);
    }

    default UriAssert and(UriStateCollector collector) {
        return then(collector);
    }

    default UrlAssert and(UrlStateCollector collector) {
        return then(collector);
    }

    default BooleanAssert and(BooleanStateCollector collector) {
        return then(collector);
    }

    default StringAssert and(StringStateCollector collector) {
        return then(collector);
    }

    default ByteAssert and(ByteStateCollector collector) {
        return then(collector);
    }

    default ByteArrayAssert and(ByteArrayStateCollector collector) {
        return then(collector);
    }

    default CharacterAssert and(CharacterStateCollector collector) {
        return then(collector);
    }

    default CharArrayAssert and(CharArrayStateCollector collector) {
        return then(collector);
    }

    default ClassAssert and(ClassStateCollector collector) {
        return then(collector);
    }

    default DoubleAssert and(DoubleStateCollector collector) {
        return then(collector);
    }

    default DoubleArrayAssert and(DoubleArrayStateCollector collector) {
        return then(collector);
    }

    default FileAssert and(FileStateCollector collector) {
        return then(collector);
    }

    default FloatAssert and(FloatStateCollector collector) {
        return then(collector);
    }

    default FloatArrayAssert and(FloatArrayStateCollector collector) {
        return then(collector);
    }

    default IntegerAssert and(IntegerStateCollector collector) {
        return then(collector);
    }

    default IntArrayAssert and(IntArrayStateCollector collector) {
        return then(collector);
    }

    default LongAssert and(LongStateCollector collector) {
        return then(collector);
    }

    default LongArrayAssert and(LongArrayStateCollector collector) {
        return then(collector);
    }

    default <T> ObjectAssert<T> and(StateCollector<T> collector) {
        return then(collector);
    }

    default <T> ObjectArrayAssert<T> and(ObjectArrayStateCollector<T> collector) {
        return then(collector);
    }

    default ShortAssert and(ShortStateCollector collector) {
        return then(collector);
    }

    default ShortArrayAssert and(ShortArrayStateCollector collector) {
        return then(collector);
    }

    default AbstractZonedDateTimeAssert<?> and(ZonedDateTimeStateCollector collector) {
        return then(collector);
    }

    default AbstractLocalDateTimeAssert<?> and(LocalDateTimeStateCollector collector) {
        return then(collector);
    }

    default AbstractOffsetDateTimeAssert<?> and(OffsetDateTimeStateCollector collector) {
        return then(collector);
    }

    default AbstractLocalTimeAssert<?> and(LocalTimeStateCollector collector) {
        return then(collector);
    }

    default AbstractLocalDateAssert<?> and(LocalDateStateCollector collector) {
        return then(collector);
    }

    default AbstractInstantAssert<?> and(InstantStateCollector collector) {
        return then(collector);
    }

    default ThrowableAssert and(ThrowableStateCollector collector) {
        return then(collector);
    }
}
