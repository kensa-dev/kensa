import * as React from 'react';
import { TestCard } from './TestCard';
import {Invocation, Test} from "@/types/Test.ts";

interface TestContainerProps {
    tests: Test[];
    testClass: string;
    testToExpand?: string;
}

export const TestContainer = ({ tests, testClass, testToExpand }: TestContainerProps) => {
    const cardRefs = React.useRef<(HTMLDivElement | null)[]>([]);
    const [expandedTestIndex, setExpandedTestIndex] = React.useState<number>(-1);
    const [expandedInvocationIndex, setExpandedInvocationIndex] = React.useState<number>(-1);

    React.useEffect(() => {
        if (tests.length === 0) return;

        const normalize = (s: string) => s.trim().toLowerCase();

        const findMethodIndex = (method: string) => {
            const target = normalize(method);
            if (!target) return -1;

            return tests.findIndex((t: Test) => {
                const candidates = [
                    t.testMethod,
                    t.displayName,
                ]
                    .filter(Boolean)
                    .map((x: string) => normalize(String(x)));

                return candidates.includes(target);
            });
        };

        let targetScrollIndex = -1;

        const methodIndex =
            testToExpand && testToExpand.length > 0 ? findMethodIndex(testToExpand) : -1;

        if (methodIndex !== -1) {
            setExpandedTestIndex(methodIndex);
            setExpandedInvocationIndex(-1);
            targetScrollIndex = methodIndex;
        } else {
            const firstFailedTestIndex = tests.findIndex(t => t.state === 'Failed');

            if (firstFailedTestIndex !== -1) {
                setExpandedTestIndex(firstFailedTestIndex);

                const failedTest = tests[firstFailedTestIndex];
                let firstFailedInvIndex = -1;
                if (failedTest.invocations?.some((inv: Invocation) => inv.parameters?.length > 0)) {
                    firstFailedInvIndex = failedTest.invocations.findIndex((inv: Invocation) => inv.state === 'Failed');
                }
                setExpandedInvocationIndex(firstFailedInvIndex);

                targetScrollIndex = firstFailedTestIndex;
            } else if (tests.length === 1) {
                setExpandedTestIndex(0);

                const singleTest = tests[0];
                let singleInvIndex = -1;
                if (singleTest.invocations?.length === 1 && !singleTest.invocations[0].parameters?.length) {
                    singleInvIndex = 0;
                }
                setExpandedInvocationIndex(singleInvIndex);

                targetScrollIndex = 0;
            } else {
                setExpandedTestIndex(-1);
                setExpandedInvocationIndex(-1);
            }
        }

        if (targetScrollIndex !== -1) {
            const timer = setTimeout(() => {
                cardRefs.current[targetScrollIndex]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 300);

            return () => clearTimeout(timer);
        }
    }, [tests, testToExpand]);

    return (
        <div className="space-y-8">
            {tests.map((test, i) => (
                <div
                    key={`${testClass}-${i}`}
                    ref={(el) => { cardRefs.current[i] = el; }}
                    className={"scroll-mt-16"}
                >
                    <TestCard
                        test={test}
                        initialExpanded={i === expandedTestIndex}
                        initialExpandedInvocation={expandedInvocationIndex}
                    />
                </div>
            ))}
        </div>
    );
};