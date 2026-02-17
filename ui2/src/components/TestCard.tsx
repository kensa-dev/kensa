import * as React from 'react';
import { Beaker, ChevronDown, ChevronRight } from 'lucide-react';
import { cn } from "@/lib/utils";
import { SectionRenderer } from './SectionRenderer';
import { InvocationCard } from "@/components/InvocationCard.tsx";
import { IssueBadge } from './IssueBadge';
import {Invocation, Test, TestState} from "@/types/Test.ts";

const cardBorder: Record<TestState, string> = {
    Passed: "border-success-30",
    Failed: "border-failure-30",
    Disabled: "border-disabled-30",
};

const headerBg: Record<TestState, string> = {
    Passed: "bg-success-10 hover:bg-success-15",
    Failed: "bg-failure-10 hover:bg-failure-15",
    Disabled: "bg-disabled-10 hover:bg-disabled-15",
};

const iconColor: Record<TestState, string> = {
    Passed: "text-success",
    Failed: "text-failure",
    Disabled: "text-disabled",
};

const bodyBg: Record<TestState, string> = {
    Passed: "bg-success-2 dark:bg-success-10",
    Failed: "bg-failure-2 dark:bg-failure-10",
    Disabled: "bg-disabled-2 dark:bg-disabled-10",
};

interface TestCardProps {
    test: Test;
    initialExpanded?: boolean;
    initialExpandedInvocation?: number;
}

export const TestCard = ({ test, initialExpanded = false, initialExpandedInvocation = -1 }: TestCardProps) => {
    const [isExpanded, setIsExpanded] = React.useState(initialExpanded);

    React.useEffect(() => {
        setIsExpanded(initialExpanded);
    }, [initialExpanded]);

    const state = test.state;
    const isDisabled = state === 'Disabled';

    return (
        <div className={cn(
            "bg-card border rounded-xl shadow-sm overflow-hidden transition-all",
            cardBorder[state]
        )}>
            <div
                className={cn(
                    "px-5 py-3 border-b flex items-center justify-between cursor-pointer select-none transition-colors",
                    headerBg[state],
                    isDisabled ? "cursor-default" : "cursor-pointer"
                )}
                onClick={() => !isDisabled && setIsExpanded(!isExpanded)}
            >
                <h3 className={"font-bold flex items-center gap-2 text-sm tracking-tight"}>
                    <Beaker size={16} className={iconColor[state]} />
                    {test.displayName}
                </h3>

                <div className="flex items-center gap-1.5 ml-4">
                    {test.issues?.map((issue: string) => (
                        <IssueBadge key={issue} issue={issue} testState={state} />
                    ))}
                    {!isDisabled && (
                        isExpanded ? <ChevronDown size={16} className="text-muted-foreground" /> : <ChevronRight size={16} className="text-muted-foreground" />
                    )}
                </div>
            </div>

            {!isDisabled && isExpanded && (
                <div className={cn(
                    "p-4 space-y-4",
                    bodyBg[state]
                )}>
                    <div className="space-y-4">
                        {test.invocations.map((invocation: Invocation, invIdx: number) => {
                            const isParameterized = invocation.parameters && invocation.parameters.length > 0;

                            if (isParameterized) {
                                return (
                                    <InvocationCard
                                        key={invIdx}
                                        invocation={invocation}
                                        autoOpenTab={test.autoOpenTab}
                                        isLast={invIdx === test.invocations.length - 1}
                                        initialExpanded={initialExpandedInvocation >= 0 ? invIdx === initialExpandedInvocation : invocation.state === 'Failed'}
                                    />
                                );
                            }

                            return (
                                <div key={invIdx} className="animate-in fade-in slide-in-from-top-1 duration-300">
                                    <SectionRenderer
                                        invocation={invocation}
                                        testState={invocation.state}
                                        autoOpenTab={test.autoOpenTab}
                                    />
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
};