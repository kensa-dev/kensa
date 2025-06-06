package dev.kensa.assertj;

import dev.kensa.StateExtractor;
import dev.kensa.StateExtractorWithFixtures;
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
    default <T, A> A then(StateExtractorWithFixtures<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.then(testContext(), extractor, assertProvider::apply);
    }

    default <T, A> A thenEventually(StateExtractorWithFixtures<T> extractor, Function<T, A> assertProvider) {
        return thenEventually(10L, SECONDS, extractor, assertProvider);
    }

    default <T, A> A thenEventually(Long timeout, ChronoUnit timeUnit, StateExtractorWithFixtures<T> extractor, Function<T, A> assertProvider) {
        return AssertJThen.thenEventually(timeout, timeUnit, testContext(), extractor, assertProvider::apply);
    }

    default <T> OptionalAssert<T> then(OptionalStateExtractorWithFixtures<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalDoubleAssert then(OptionalDoubleStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalIntAssert then(OptionalIntStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalLongAssert then(OptionalLongStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default BigDecimalAssert then(BigDecimalStateExtractorWithFixtures extractor) {
        return then(extractor, BigDecimalAssert::new);
    }

    default BigIntegerAssert then(BigIntegerStateExtractorWithFixtures extractor) {
        return then(extractor, BigIntegerAssert::new);
    }

    default UriAssert then(UriStateExtractorWithFixtures extractor) {
        return then(extractor, UriAssert::new);
    }

    default UrlAssert then(UrlStateExtractorWithFixtures extractor) {
        return then(extractor, UrlAssert::new);
    }

    default BooleanAssert then(BooleanStateExtractorWithFixtures extractor) {
        return then(extractor, BooleanAssert::new);
    }

    default StringAssert then(StringStateExtractorWithFixtures extractor) {
        return then(extractor, StringAssert::new);
    }

    default ByteAssert then(ByteStateExtractorWithFixtures extractor) {
        return then(extractor, ByteAssert::new);
    }

    default ByteArrayAssert then(ByteArrayStateExtractorWithFixtures extractor) {
        return then(extractor, ByteArrayAssert::new);
    }

    default CharacterAssert then(CharacterStateExtractorWithFixtures extractor) {
        return then(extractor, CharacterAssert::new);
    }

    default CharArrayAssert then(CharArrayStateExtractorWithFixtures extractor) {
        return then(extractor, CharArrayAssert::new);
    }

    default ClassAssert then(ClassStateExtractorWithFixtures extractor) {
        return then(extractor, ClassAssert::new);
    }

    default DoubleAssert then(DoubleStateExtractorWithFixtures extractor) {
        return then(extractor, DoubleAssert::new);
    }

    default DoubleArrayAssert then(DoubleArrayStateExtractorWithFixtures extractor) {
        return then(extractor, DoubleArrayAssert::new);
    }

    default FileAssert then(FileStateExtractorWithFixtures extractor) {
        return then(extractor, FileAssert::new);
    }

    default FloatAssert then(FloatStateExtractorWithFixtures extractor) {
        return then(extractor, FloatAssert::new);
    }

    default FloatArrayAssert then(FloatArrayStateExtractorWithFixtures extractor) {
        return then(extractor, FloatArrayAssert::new);
    }

    default IntegerAssert then(IntegerStateExtractorWithFixtures extractor) {
        return then(extractor, IntegerAssert::new);
    }

    default IntArrayAssert then(IntArrayStateExtractorWithFixtures extractor) {
        return then(extractor, IntArrayAssert::new);
    }

    default LongAssert then(LongStateExtractorWithFixtures extractor) {
        return then(extractor, LongAssert::new);
    }

    default LongArrayAssert then(LongArrayStateExtractorWithFixtures extractor) {
        return then(extractor, LongArrayAssert::new);
    }

    default <T> ObjectAssert<T> then(StateExtractorWithFixtures<T> extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default <T> ObjectArrayAssert<T> then(ObjectArrayStateExtractorWithFixtures<T> extractor) {
        return then(extractor, ObjectArrayAssert::new);
    }

    default ShortAssert then(ShortStateExtractorWithFixtures extractor) {
        return then(extractor, ShortAssert::new);
    }

    default ShortArrayAssert then(ShortArrayStateExtractorWithFixtures extractor) {
        return then(extractor, ShortArrayAssert::new);
    }

    default AbstractZonedDateTimeAssert<?> then(ZonedDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateTimeAssert<?> then(LocalDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractOffsetDateTimeAssert<?> then(OffsetDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalTimeAssert<?> then(LocalTimeStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateAssert<?> then(LocalDateStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractInstantAssert<?> then(InstantStateExtractorWithFixtures extractor) {
        return then(extractor, AssertionsForClassTypes::assertThat);
    }

    default ThrowableAssert then(ThrowableStateExtractorWithFixtures extractor) {
        return then(extractor, ThrowableAssert::new);
    }

    default <T> OptionalAssert<T> and(OptionalStateExtractorWithFixtures<T> extractor) {
        return then(extractor);
    }

    default OptionalDoubleAssert and(OptionalDoubleStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default OptionalIntAssert and(OptionalIntStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default OptionalLongAssert and(OptionalLongStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default BigDecimalAssert and(BigDecimalStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default BigIntegerAssert and(BigIntegerStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default UriAssert and(UriStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default UrlAssert and(UrlStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default BooleanAssert and(BooleanStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default StringAssert and(StringStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default ByteAssert and(ByteStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default ByteArrayAssert and(ByteArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default CharacterAssert and(CharacterStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default CharArrayAssert and(CharArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default ClassAssert and(ClassStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default DoubleAssert and(DoubleStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default DoubleArrayAssert and(DoubleArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default FileAssert and(FileStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default FloatAssert and(FloatStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default FloatArrayAssert and(FloatArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default IntegerAssert and(IntegerStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default IntArrayAssert and(IntArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default LongAssert and(LongStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default LongArrayAssert and(LongArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default <T> ObjectAssert<T> and(StateExtractorWithFixtures<T> extractor) {
        return then(extractor);
    }

    default <T> ObjectArrayAssert<T> and(ObjectArrayStateExtractorWithFixtures<T> extractor) {
        return then(extractor);
    }

    default ShortAssert and(ShortStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default ShortArrayAssert and(ShortArrayStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractZonedDateTimeAssert<?> and(ZonedDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractLocalDateTimeAssert<?> and(LocalDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractOffsetDateTimeAssert<?> and(OffsetDateTimeStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractLocalTimeAssert<?> and(LocalTimeStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractLocalDateAssert<?> and(LocalDateStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default AbstractInstantAssert<?> and(InstantStateExtractorWithFixtures extractor) {
        return then(extractor);
    }

    default ThrowableAssert and(ThrowableStateExtractorWithFixtures extractor) {
        return then(extractor);
    }
}
