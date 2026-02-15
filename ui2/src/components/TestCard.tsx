import * as React from 'react';
import { Beaker, ChevronDown, ChevronRight } from 'lucide-react';
import { cn } from "@/lib/utils";
import { SectionRenderer } from './SectionRenderer';
import { InvocationCard } from "@/components/InvocationCard.tsx";
import { IssueBadge } from './IssueBadge';
import {Invocation, Test} from "@/types/Test.ts";

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

    const isPassed = test.state === 'Passed';

    return (
        <div className={cn(
            "bg-card border rounded-xl shadow-sm overflow-hidden transition-all",
            isPassed ? "border-success-30" : "border-failure-30"
        )}>
            <div
                className={cn(
                    "px-5 py-3 border-b flex items-center justify-between cursor-pointer select-none transition-colors",
                    isPassed ? "bg-success-10 hover:bg-success-15" : "bg-failure-10 hover:bg-failure-15"
                )}
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <h3 className={"font-bold flex items-center gap-2 text-sm tracking-tight"}>
                    <Beaker size={16} className={isPassed ? "text-success" : "text-failure"} />
                    {test.displayName}
                </h3>

                <div className="flex items-center gap-1.5 ml-4">
                    {test.issues?.map((issue: string) => (
                        <IssueBadge key={issue} issue={issue} testState={test.state} />
                    ))}
                    {isExpanded ? <ChevronDown size={16} className="text-muted-foreground" /> : <ChevronRight size={16} className="text-muted-foreground" />}
                </div>
            </div>

            {isExpanded && (
                <div className={cn(
                    "p-6 space-y-4",
                    isPassed ? "bg-success-2 dark:bg-success-10" : "bg-failure-2 dark:bg-failure-10"
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