import * as React from 'react';
import {CheckCircle2, ChevronDown, ChevronRight, CircleMinus, XCircle} from 'lucide-react';
import {cn} from "@/lib/utils";
import {SectionRenderer} from './SectionRenderer';
import {InvocationCard} from "@/components/InvocationCard";
import {IssueBadge} from './IssueBadge';
import {Invocation, Test, TestState} from "@/types/Test";

const cardBorder: Record<TestState, string> = {
    Passed: "border-success/40",
    Failed: "border-failure/40",
    Disabled: "border-disabled/35",
    "Not Executed": "border-border",
};

const headerBg: Record<TestState, string> = {
    Passed: "bg-success/[0.09] hover:bg-success/[0.14]",
    Failed: "bg-failure/[0.09] hover:bg-failure/[0.14]",
    Disabled: "bg-disabled/[0.09] hover:bg-disabled/[0.14]",
    "Not Executed": "bg-muted/50",
};

const iconColor: Record<TestState, string> = {
    Passed: "text-success",
    Failed: "text-failure",
    Disabled: "text-disabled",
    "Not Executed": "text-muted-foreground",
};

const StateIcon = ({ state, size = 16 }: { state: TestState; size?: number }) => {
    const cls = iconColor[state];
    if (state === 'Failed')   return <XCircle size={size} className={cls} />;
    if (state === 'Disabled') return <CircleMinus size={size} className={cls} />;
    return <CheckCircle2 size={size} className={cls} />;
};

const bodyBg: Record<TestState, string> = {
    Passed: "bg-success/[0.04] dark:bg-success/[0.08]",
    Failed: "bg-failure/[0.04] dark:bg-failure/[0.08]",
    Disabled: "bg-disabled/[0.04] dark:bg-disabled/[0.06]",
    "Not Executed": "bg-muted/30",
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
                    <StateIcon state={state} />
                    {test.displayName}
                </h3>

                <div className="flex items-center gap-1.5 ml-4">
                    {test.issues?.map((issue: string) => (
                        <IssueBadge key={issue} issue={issue} testState={state} />
                    ))}
                    {!isDisabled && !isExpanded && test.invocations.length > 1 && (
                        <span className="text-[10px] font-mono text-muted-foreground/60 tabular-nums">
                            {test.invocations.length}×
                        </span>
                    )}
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
                                        parseErrors={test.parseErrors}
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
                                        parseErrors={test.parseErrors}
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