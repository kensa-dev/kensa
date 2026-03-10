import * as React from 'react';
import { TestCard } from './TestCard';
import {Invocation, Test} from "@/types/Test";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";

interface TestContainerProps {
    tests: Test[];
    testClass: string;
    testToExpand?: string;
    matchingMethods?: string[];
    onClearFilter?: () => void;
}

export const TestContainer = ({ tests, testClass, testToExpand, matchingMethods = [], onClearFilter }: TestContainerProps) => {
    const cardRefs = React.useRef<(HTMLDivElement | null)[]>([]);
    const [expandedTestIndex, setExpandedTestIndex] = React.useState<number>(-1);
    const [expandedInvocationIndex, setExpandedInvocationIndex] = React.useState<number>(-1);

    // Filter tests to only show matching methods if filter is active
    const filteredTests = React.useMemo(() => {
        if (matchingMethods.length === 0) {
            return tests;
        }
        return tests.filter(test => matchingMethods.includes(test.testMethod));
    }, [tests, matchingMethods]);

    React.useEffect(() => {
        if (filteredTests.length === 0) return;

        const normalize = (s: string) => s.trim().toLowerCase();

        const findMethodIndex = (method: string) => {
            const target = normalize(method);
            if (!target) return -1;

            return filteredTests.findIndex((t: Test) => {
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
            const firstFailedTestIndex = filteredTests.findIndex(t => t.state === 'Failed');

            if (firstFailedTestIndex !== -1) {
                setExpandedTestIndex(firstFailedTestIndex);

                const failedTest = filteredTests[firstFailedTestIndex];
                let firstFailedInvIndex = -1;
                if (failedTest.invocations?.some((inv: Invocation) => inv.parameters?.length > 0)) {
                    firstFailedInvIndex = failedTest.invocations.findIndex((inv: Invocation) => inv.state === 'Failed');
                }
                setExpandedInvocationIndex(firstFailedInvIndex);

                targetScrollIndex = firstFailedTestIndex;
            } else if (filteredTests.length === 1) {
                setExpandedTestIndex(0);

                const singleTest = filteredTests[0];
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
    }, [filteredTests, testToExpand]);

    const filteredCount = tests.length - filteredTests.length;

    return (
        <div className="space-y-4">
            {filteredCount > 0 && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
                    <span>{filteredCount} {filteredCount === 1 ? 'test' : 'tests'} hidden by filter</span>
                    {onClearFilter && (
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={onClearFilter}
                            className="h-6 px-2 text-xs"
                        >
                            <X className="h-3 w-3 mr-1" />
                            Show all
                        </Button>
                    )}
                </div>
            )}
            {filteredTests.map((test, i) => (
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