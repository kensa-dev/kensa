import { describe, it, expect } from 'vitest';
import { getFailingLine } from './stackTrace';

describe('getFailingLine', () => {
    it('returns undefined when no stack frame matches sentenceLines', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat features.MyTest.myMethod(MyTest.kt:50)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91]), 'features.MyTest')).toBeUndefined();
    });

    it('returns the matching line when there is a single match', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91]), 'features.MyTest')).toBe(91);
    });

    it('matches the test-class frame and ignores framework frames around it', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat io.kotest.assertions.nondeterministic.EventuallyKt.runIterations(eventually.kt:114)',
            '\tat io.kotest.assertions.nondeterministic.EventuallyKt.eventually(eventually.kt:80)',
            '\tat features.provisioning.OrderTest.handlesNotification(OrderTest.kt:91)',
            '\tat org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:142)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([69, 77, 91, 114]), 'features.provisioning.OrderTest')).toBe(91);
    });

    it('skips java stdlib frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat java.base/java.lang.reflect.Method.invoke(Method.java:568)',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 568]), 'features.MyTest')).toBe(91);
    });

    it('skips kotlin stdlib frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 33]), 'features.MyTest')).toBe(91);
    });

    it('skips org.junit frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
            '\tat org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:142)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 142]), 'features.MyTest')).toBe(91);
    });

    it('skips dev.kensa internal frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
            '\tat dev.kensa.junit.KensaExtension.afterEach(KensaExtension.kt:55)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 55]), 'features.MyTest')).toBe(91);
    });

    it('ignores frames from non-test classes whose line numbers happen to coincide with sentence lines', () => {
        // Regression: a Caused-by frame in a different class with a line number that
        // happens to coincide with a sentence line was overwriting the correct test-class frame.
        const trace = [
            'org.opentest4j.AssertionFailedError: boom',
            '\tat io.kotest.assertions.AssertionErrorBuilder_jvmKt.createAssertionError(AssertionErrorBuilder.jvm.kt:30)',
            '\tat com.example.WidgetTest.handlesWidgetCorrectly(WidgetTest.kt:42)',
            'Caused by: java.lang.IllegalStateException: missing widget',
            '\tat com.example.support.WidgetMatcher$check$1.invoke(WidgetMatcher.kt:25)',
        ].join('\n');
        const sentenceLines = new Set([20, 25, 30, 42, 55]);
        expect(getFailingLine(trace, sentenceLines, 'com.example.WidgetTest')).toBe(42);
    });

    it('matches frames from inner classes / lambdas of the test class', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError',
            '\tat features.MyTest$lambda$0.invoke(MyTest.kt:42)',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([42, 91]), 'features.MyTest')).toBe(91);
    });

    it('does not match frames from a different class with a similar prefix', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError',
            '\tat features.MyTestHelper.help(MyTestHelper.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91]), 'features.MyTest')).toBeUndefined();
    });
});
