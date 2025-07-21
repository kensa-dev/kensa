package dev.kensa.assertj;

import dev.kensa.StateCollector;
import dev.kensa.StateExtractor;
import org.assertj.core.api.*;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import static dev.kensa.context.TestContextHolder.testContext;
import static java.time.temporal.ChronoUnit.SECONDS;

public interface WithAssertJ {

    default <T, A> A then(StateExtractor<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.then(testContext(), extractor, assertProvider::apply);
    }

    default <T, A> A thenEventually(StateExtractor<T> extractor, Function<T, A> assertProvider) {
        return thenEventually(10L, SECONDS, extractor, assertProvider);
    }

    default <T, A> A thenEventually(Long timeout, ChronoUnit timeUnit, StateExtractor<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.thenEventually(timeout, timeUnit, testContext(), extractor, assertProvider::apply);
    }

    default <T> OptionalAssert<T> then(OptionalStateExtractor<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalDoubleAssert then(OptionalDoubleStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalIntAssert then(OptionalIntStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalLongAssert then(OptionalLongStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default BigDecimalAssert then(BigDecimalStateExtractor extractor) {
        return then(extractor, BigDecimalAssert::new);
    }

    default BigIntegerAssert then(BigIntegerStateExtractor extractor) {
        return then(extractor, BigIntegerAssert::new);
    }

    default UriAssert then(UriStateExtractor extractor) {
        return then(extractor, UriAssert::new);
    }

    default UrlAssert then(UrlStateExtractor extractor) {
        return then(extractor, UrlAssert::new);
    }

    default BooleanAssert then(BooleanStateExtractor extractor) {
        return then(extractor, BooleanAssert::new);
    }

    default StringAssert then(StringStateExtractor extractor) {
        return then(extractor, StringAssert::new);
    }

    default ByteAssert then(ByteStateExtractor extractor) {
        return then(extractor, ByteAssert::new);
    }

    default ByteArrayAssert then(ByteArrayStateExtractor extractor) {
        return then(extractor, ByteArrayAssert::new);
    }

    default CharacterAssert then(CharacterStateExtractor extractor) {
        return then(extractor, CharacterAssert::new);
    }

    default CharArrayAssert then(CharArrayStateExtractor extractor) {
        return then(extractor, CharArrayAssert::new);
    }

    default ClassAssert then(ClassStateExtractor extractor) {
        return then(extractor, ClassAssert::new);
    }

    default DoubleAssert then(DoubleStateExtractor extractor) {
        return then(extractor, DoubleAssert::new);
    }

    default DoubleArrayAssert then(DoubleArrayStateExtractor extractor) {
        return then(extractor, DoubleArrayAssert::new);
    }

    default FileAssert then(FileStateExtractor extractor) {
        return then(extractor, FileAssert::new);
    }

    default FloatAssert then(FloatStateExtractor extractor) {
        return then(extractor, FloatAssert::new);
    }

    default FloatArrayAssert then(FloatArrayStateExtractor extractor) {
        return then(extractor, FloatArrayAssert::new);
    }

    default IntegerAssert then(IntegerStateExtractor extractor) {
        return then(extractor, IntegerAssert::new);
    }

    default IntArrayAssert then(IntArrayStateExtractor extractor) {
        return then(extractor, IntArrayAssert::new);
    }

    default LongAssert then(LongStateExtractor extractor) {
        return then(extractor, LongAssert::new);
    }

    default LongArrayAssert then(LongArrayStateExtractor extractor) {
        return then(extractor, LongArrayAssert::new);
    }

    default <T> ObjectAssert<T> then(StateExtractor<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default <T> ObjectArrayAssert<T> then(ObjectArrayStateExtractor<T> extractor) {
        return then(extractor, ObjectArrayAssert::new);
    }

    default ShortAssert then(ShortStateExtractor extractor) {
        return then(extractor, ShortAssert::new);
    }

    default ShortArrayAssert then(ShortArrayStateExtractor extractor) {
        return then(extractor, ShortArrayAssert::new);
    }

    default AbstractZonedDateTimeAssert<?> then(ZonedDateTimeStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateTimeAssert<?> then(LocalDateTimeStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractOffsetDateTimeAssert<?> then(OffsetDateTimeStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalTimeAssert<?> then(LocalTimeStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateAssert<?> then(LocalDateStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractInstantAssert<?> then(InstantStateExtractor extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default ThrowableAssert then(ThrowableStateExtractor extractor) {
        return then(extractor, ThrowableAssert::new);
    }

    default <T> OptionalAssert<T> and(OptionalStateExtractor<T> extractor) {
        return then(extractor);
    }

    default OptionalDoubleAssert and(OptionalDoubleStateExtractor extractor) {
        return then(extractor);
    }

    default OptionalIntAssert and(OptionalIntStateExtractor extractor) {
        return then(extractor);
    }

    default OptionalLongAssert and(OptionalLongStateExtractor extractor) {
        return then(extractor);
    }

    default BigDecimalAssert and(BigDecimalStateExtractor extractor) {
        return then(extractor);
    }

    default BigIntegerAssert and(BigIntegerStateExtractor extractor) {
        return then(extractor);
    }

    default UriAssert and(UriStateExtractor extractor) {
        return then(extractor);
    }

    default UrlAssert and(UrlStateExtractor extractor) {
        return then(extractor);
    }

    default BooleanAssert and(BooleanStateExtractor extractor) {
        return then(extractor);
    }

    default StringAssert and(StringStateExtractor extractor) {
        return then(extractor);
    }

    default ByteAssert and(ByteStateExtractor extractor) {
        return then(extractor);
    }

    default ByteArrayAssert and(ByteArrayStateExtractor extractor) {
        return then(extractor);
    }

    default CharacterAssert and(CharacterStateExtractor extractor) {
        return then(extractor);
    }

    default CharArrayAssert and(CharArrayStateExtractor extractor) {
        return then(extractor);
    }

    default ClassAssert and(ClassStateExtractor extractor) {
        return then(extractor);
    }

    default DoubleAssert and(DoubleStateExtractor extractor) {
        return then(extractor);
    }

    default DoubleArrayAssert and(DoubleArrayStateExtractor extractor) {
        return then(extractor);
    }

    default FileAssert and(FileStateExtractor extractor) {
        return then(extractor);
    }

    default FloatAssert and(FloatStateExtractor extractor) {
        return then(extractor);
    }

    default FloatArrayAssert and(FloatArrayStateExtractor extractor) {
        return then(extractor);
    }

    default IntegerAssert and(IntegerStateExtractor extractor) {
        return then(extractor);
    }

    default IntArrayAssert and(IntArrayStateExtractor extractor) {
        return then(extractor);
    }

    default LongAssert and(LongStateExtractor extractor) {
        return then(extractor);
    }

    default LongArrayAssert and(LongArrayStateExtractor extractor) {
        return then(extractor);
    }

    default <T> ObjectAssert<T> and(StateExtractor<T> extractor) {
        return then(extractor);
    }

    default <T> ObjectArrayAssert<T> and(ObjectArrayStateExtractor<T> extractor) {
        return then(extractor);
    }

    default ShortAssert and(ShortStateExtractor extractor) {
        return then(extractor);
    }

    default ShortArrayAssert and(ShortArrayStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractZonedDateTimeAssert<?> and(ZonedDateTimeStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractLocalDateTimeAssert<?> and(LocalDateTimeStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractOffsetDateTimeAssert<?> and(OffsetDateTimeStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractLocalTimeAssert<?> and(LocalTimeStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractLocalDateAssert<?> and(LocalDateStateExtractor extractor) {
        return then(extractor);
    }

    default AbstractInstantAssert<?> and(InstantStateExtractor extractor) {
        return then(extractor);
    }

    default ThrowableAssert and(ThrowableStateExtractor extractor) {
        return then(extractor);
    }



    default <T, A> A then(StateCollector<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.then(testContext(), extractor, assertProvider::apply);
    }

    default <T, A> A thenEventually(StateCollector<T> extractor, Function<T, A> assertProvider) {
        return thenEventually(10L, SECONDS, extractor, assertProvider);
    }

    default <T, A> A thenEventually(Long timeout, ChronoUnit timeUnit, StateCollector<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.thenEventually(timeout, timeUnit, testContext(), extractor, assertProvider::apply);
    }

    default <T> OptionalAssert<T> then(OptionalStateCollector<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalDoubleAssert then(OptionalDoubleStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalIntAssert then(OptionalIntStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalLongAssert then(OptionalLongStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default BigDecimalAssert then(BigDecimalStateCollector extractor) {
        return then(extractor, BigDecimalAssert::new);
    }

    default BigIntegerAssert then(BigIntegerStateCollector extractor) {
        return then(extractor, BigIntegerAssert::new);
    }

    default UriAssert then(UriStateCollector extractor) {
        return then(extractor, UriAssert::new);
    }

    default UrlAssert then(UrlStateCollector extractor) {
        return then(extractor, UrlAssert::new);
    }

    default BooleanAssert then(BooleanStateCollector extractor) {
        return then(extractor, BooleanAssert::new);
    }

    default StringAssert then(StringStateCollector extractor) {
        return then(extractor, StringAssert::new);
    }

    default ByteAssert then(ByteStateCollector extractor) {
        return then(extractor, ByteAssert::new);
    }

    default ByteArrayAssert then(ByteArrayStateCollector extractor) {
        return then(extractor, ByteArrayAssert::new);
    }

    default CharacterAssert then(CharacterStateCollector extractor) {
        return then(extractor, CharacterAssert::new);
    }

    default CharArrayAssert then(CharArrayStateCollector extractor) {
        return then(extractor, CharArrayAssert::new);
    }

    default ClassAssert then(ClassStateCollector extractor) {
        return then(extractor, ClassAssert::new);
    }

    default DoubleAssert then(DoubleStateCollector extractor) {
        return then(extractor, DoubleAssert::new);
    }

    default DoubleArrayAssert then(DoubleArrayStateCollector extractor) {
        return then(extractor, DoubleArrayAssert::new);
    }

    default FileAssert then(FileStateCollector extractor) {
        return then(extractor, FileAssert::new);
    }

    default FloatAssert then(FloatStateCollector extractor) {
        return then(extractor, FloatAssert::new);
    }

    default FloatArrayAssert then(FloatArrayStateCollector extractor) {
        return then(extractor, FloatArrayAssert::new);
    }

    default IntegerAssert then(IntegerStateCollector extractor) {
        return then(extractor, IntegerAssert::new);
    }

    default IntArrayAssert then(IntArrayStateCollector extractor) {
        return then(extractor, IntArrayAssert::new);
    }

    default LongAssert then(LongStateCollector extractor) {
        return then(extractor, LongAssert::new);
    }

    default LongArrayAssert then(LongArrayStateCollector extractor) {
        return then(extractor, LongArrayAssert::new);
    }

    default <T> ObjectAssert<T> then(StateCollector<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default <T> ObjectArrayAssert<T> then(ObjectArrayStateCollector<T> extractor) {
        return then(extractor, ObjectArrayAssert::new);
    }

    default ShortAssert then(ShortStateCollector extractor) {
        return then(extractor, ShortAssert::new);
    }

    default ShortArrayAssert then(ShortArrayStateCollector extractor) {
        return then(extractor, ShortArrayAssert::new);
    }

    default AbstractZonedDateTimeAssert<?> then(ZonedDateTimeStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateTimeAssert<?> then(LocalDateTimeStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractOffsetDateTimeAssert<?> then(OffsetDateTimeStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalTimeAssert<?> then(LocalTimeStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateAssert<?> then(LocalDateStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractInstantAssert<?> then(InstantStateCollector extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default ThrowableAssert then(ThrowableStateCollector extractor) {
        return then(extractor, ThrowableAssert::new);
    }

    default <T> OptionalAssert<T> and(OptionalStateCollector<T> extractor) {
        return then(extractor);
    }

    default OptionalDoubleAssert and(OptionalDoubleStateCollector extractor) {
        return then(extractor);
    }

    default OptionalIntAssert and(OptionalIntStateCollector extractor) {
        return then(extractor);
    }

    default OptionalLongAssert and(OptionalLongStateCollector extractor) {
        return then(extractor);
    }

    default BigDecimalAssert and(BigDecimalStateCollector extractor) {
        return then(extractor);
    }

    default BigIntegerAssert and(BigIntegerStateCollector extractor) {
        return then(extractor);
    }

    default UriAssert and(UriStateCollector extractor) {
        return then(extractor);
    }

    default UrlAssert and(UrlStateCollector extractor) {
        return then(extractor);
    }

    default BooleanAssert and(BooleanStateCollector extractor) {
        return then(extractor);
    }

    default StringAssert and(StringStateCollector extractor) {
        return then(extractor);
    }

    default ByteAssert and(ByteStateCollector extractor) {
        return then(extractor);
    }

    default ByteArrayAssert and(ByteArrayStateCollector extractor) {
        return then(extractor);
    }

    default CharacterAssert and(CharacterStateCollector extractor) {
        return then(extractor);
    }

    default CharArrayAssert and(CharArrayStateCollector extractor) {
        return then(extractor);
    }

    default ClassAssert and(ClassStateCollector extractor) {
        return then(extractor);
    }

    default DoubleAssert and(DoubleStateCollector extractor) {
        return then(extractor);
    }

    default DoubleArrayAssert and(DoubleArrayStateCollector extractor) {
        return then(extractor);
    }

    default FileAssert and(FileStateCollector extractor) {
        return then(extractor);
    }

    default FloatAssert and(FloatStateCollector extractor) {
        return then(extractor);
    }

    default FloatArrayAssert and(FloatArrayStateCollector extractor) {
        return then(extractor);
    }

    default IntegerAssert and(IntegerStateCollector extractor) {
        return then(extractor);
    }

    default IntArrayAssert and(IntArrayStateCollector extractor) {
        return then(extractor);
    }

    default LongAssert and(LongStateCollector extractor) {
        return then(extractor);
    }

    default LongArrayAssert and(LongArrayStateCollector extractor) {
        return then(extractor);
    }

    default <T> ObjectAssert<T> and(StateCollector<T> extractor) {
        return then(extractor);
    }

    default <T> ObjectArrayAssert<T> and(ObjectArrayStateCollector<T> extractor) {
        return then(extractor);
    }

    default ShortAssert and(ShortStateCollector extractor) {
        return then(extractor);
    }

    default ShortArrayAssert and(ShortArrayStateCollector extractor) {
        return then(extractor);
    }

    default AbstractZonedDateTimeAssert<?> and(ZonedDateTimeStateCollector extractor) {
        return then(extractor);
    }

    default AbstractLocalDateTimeAssert<?> and(LocalDateTimeStateCollector extractor) {
        return then(extractor);
    }

    default AbstractOffsetDateTimeAssert<?> and(OffsetDateTimeStateCollector extractor) {
        return then(extractor);
    }

    default AbstractLocalTimeAssert<?> and(LocalTimeStateCollector extractor) {
        return then(extractor);
    }

    default AbstractLocalDateAssert<?> and(LocalDateStateCollector extractor) {
        return then(extractor);
    }

    default AbstractInstantAssert<?> and(InstantStateCollector extractor) {
        return then(extractor);
    }

    default ThrowableAssert and(ThrowableStateCollector extractor) {
        return then(extractor);
    }
}
