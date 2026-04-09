import { describe, it, expect } from 'vitest';
import { getFailingLine } from './stackTrace';

describe('getFailingLine', () => {
    it('returns undefined when no stack frame matches sentenceLines', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat features.MyTest.myMethod(MyTest.kt:50)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91]))).toBeUndefined();
    });

    it('returns the matching line when there is a single match', () => {
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91]))).toBe(91);
    });

    it('returns the last match, not the first, when a framework frame precedes the test frame', () => {
        // Reproduces the original bug: kotest eventually.kt:114 appeared before the test line 91
        const trace = [
            'org.opentest4j.AssertionFailedError: expected: 1 but was: 2',
            '\tat io.kotest.assertions.nondeterministic.EventuallyKt.runIterations(eventually.kt:114)',
            '\tat io.kotest.assertions.nondeterministic.EventuallyKt.eventually(eventually.kt:80)',
            '\tat features.provisioning.OrderTest.handlesNotification(OrderTest.kt:91)',
            '\tat org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:142)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([69, 77, 91, 114]))).toBe(91);
    });

    it('skips java stdlib frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat java.base/java.lang.reflect.Method.invoke(Method.java:568)',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 568]))).toBe(91);
    });

    it('skips kotlin stdlib frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 33]))).toBe(91);
    });

    it('skips org.junit frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
            '\tat org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:142)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 142]))).toBe(91);
    });

    it('skips dev.kensa internal frames', () => {
        const trace = [
            'java.lang.AssertionError',
            '\tat features.MyTest.myMethod(MyTest.kt:91)',
            '\tat dev.kensa.junit.KensaExtension.afterEach(KensaExtension.kt:55)',
        ].join('\n');
        expect(getFailingLine(trace, new Set([91, 55]))).toBe(91);
    });
});
