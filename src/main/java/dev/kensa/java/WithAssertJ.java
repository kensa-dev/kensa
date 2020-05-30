package dev.kensa.java;

import dev.kensa.StateExtractor;
import org.assertj.core.api.*;

import static dev.kensa.context.TestContextHolder.testContext;

public interface WithAssertJ {

    default <T> OptionalAssert<T> then(OptionalStateExtractor<T> extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalDoubleAssert then(OptionalDoubleStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalIntAssert then(OptionalIntStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default OptionalLongAssert then(OptionalLongStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default BigDecimalAssert then(BigDecimalStateExtractor extractor) {
        return testContext().then(extractor, BigDecimalAssert::new);
    }

    default BigIntegerAssert then(BigIntegerStateExtractor extractor) {
        return testContext().then(extractor, BigIntegerAssert::new);
    }

    default UriAssert then(UriStateExtractor extractor) {
        return testContext().then(extractor, UriAssert::new);
    }

    default UrlAssert then(UrlStateExtractor extractor) {
        return testContext().then(extractor, UrlAssert::new);
    }

    default BooleanAssert then(BooleanStateExtractor extractor) {
        return testContext().then(extractor, BooleanAssert::new);
    }

    default StringAssert then(StringStateExtractor extractor) {
        return testContext().then(extractor, StringAssert::new);
    }

    default ByteAssert then(ByteStateExtractor extractor) {
        return testContext().then(extractor, ByteAssert::new);
    }

    default ByteArrayAssert then(ByteArrayStateExtractor extractor) {
        return testContext().then(extractor, ByteArrayAssert::new);
    }

    default CharacterAssert then(CharacterStateExtractor extractor) {
        return testContext().then(extractor, CharacterAssert::new);
    }

    default CharArrayAssert then(CharArrayStateExtractor extractor) {
        return testContext().then(extractor, CharArrayAssert::new);
    }

    default ClassAssert then(ClassStateExtractor extractor) {
        return testContext().then(extractor, ClassAssert::new);
    }

    default DoubleAssert then(DoubleStateExtractor extractor) {
        return testContext().then(extractor, DoubleAssert::new);
    }

    default DoubleArrayAssert then(DoubleArrayStateExtractor extractor) {
        return testContext().then(extractor, DoubleArrayAssert::new);
    }

    default FileAssert then(FileStateExtractor extractor) {
        return testContext().then(extractor, FileAssert::new);
    }

    default FloatAssert then(FloatStateExtractor extractor) {
        return testContext().then(extractor, FloatAssert::new);
    }

    default FloatArrayAssert then(FloatArrayStateExtractor extractor) {
        return testContext().then(extractor, FloatArrayAssert::new);
    }

    default IntegerAssert then(IntegerStateExtractor extractor) {
        return testContext().then(extractor, IntegerAssert::new);
    }

    default IntArrayAssert then(IntArrayStateExtractor extractor) {
        return testContext().then(extractor, IntArrayAssert::new);
    }

    default LongAssert then(LongStateExtractor extractor) {
        return testContext().then(extractor, LongAssert::new);
    }

    default LongArrayAssert then(LongArrayStateExtractor extractor) {
        return testContext().then(extractor, LongArrayAssert::new);
    }

    default <T> ObjectAssert<T> then(StateExtractor<T> extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default <T> ObjectArrayAssert<T> then(ObjectArrayStateExtractor<T> extractor) {
        return testContext().then(extractor, ObjectArrayAssert::new);
    }

    default ShortAssert then(ShortStateExtractor extractor) {
        return testContext().then(extractor, ShortAssert::new);
    }

    default ShortArrayAssert then(ShortArrayStateExtractor extractor) {
        return testContext().then(extractor, ShortArrayAssert::new);
    }

    default AbstractZonedDateTimeAssert<?> then(ZonedDateTimeStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateTimeAssert<?> then(LocalDateTimeStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractOffsetDateTimeAssert<?> then(OffsetDateTimeStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalTimeAssert<?> then(LocalTimeStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractLocalDateAssert<?> then(LocalDateStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default AbstractInstantAssert<?> then(InstantStateExtractor extractor) {
        return testContext().then(extractor, AssertionsForClassTypes::assertThat);
    }

    default ThrowableAssert then(ThrowableStateExtractor extractor) {
        return testContext().then(extractor, ThrowableAssert::new);
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
}
