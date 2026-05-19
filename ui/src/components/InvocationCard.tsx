import * as React from 'react';
import {ChevronDown, ChevronRight} from 'lucide-react';
import {cn} from "@/lib/utils";
import {SectionRenderer} from './SectionRenderer';
import {InvocationParameters} from './InvocationParameters';
import {Invocation, ParseError} from "@/types/Test";
import {summarizeInvocation} from "@/util/invocationSummary";

interface InvocationCardProps {
    invocation: Invocation;
    parseErrors?: ParseError[];
    autoOpenTab?: string;
    isLast: boolean;
    expanded: boolean;
    onToggle: () => void;
    testClass: string;
}

export const InvocationCard = React.forwardRef<HTMLDivElement, InvocationCardProps>(
    ({invocation, parseErrors, autoOpenTab, isLast, expanded, onToggle, testClass}, ref) => {
        const isPassed = invocation.state === 'Passed';
        const summary = summarizeInvocation(invocation);

        return (
            <div
                ref={ref}
                className={cn(
                    "border rounded-lg overflow-hidden transition-all duration-200 bg-background",
                    !isLast && "mb-3",
                    isPassed ? "border-success/40 shadow-sm" : "border-failure/40 shadow-sm"
                )}>
                <div
                    className={cn(
                        "px-4 py-2 flex items-center justify-between cursor-pointer select-none transition-colors",
                        isPassed ? "hover:bg-success/10 text-neutral-800 dark:text-neutral-100" : "hover:bg-failure/10 text-neutral-800 dark:text-neutral-100"
                    )}
                    onClick={onToggle}
                >
                    <div className="flex items-center gap-3 overflow-hidden">
                        {expanded ? <ChevronDown size={14} className="shrink-0 opacity-50"/> : <ChevronRight size={14} className="shrink-0 opacity-50"/>}
                        {summary.kind === 'chip' ? (
                            <span className="text-[10px] px-1.5 py-0.5 rounded-md bg-muted/60 text-muted-foreground font-bold shrink-0">
                                {summary.count} params
                            </span>
                        ) : (
                            <span className="text-[11px] font-mono tracking-tight font-semibold">
                                {summary.text}
                            </span>
                        )}
                    </div>
                    {invocation.elapsedTime && <span className="text-[10px] font-mono opacity-40">{invocation.elapsedTime}</span>}
                </div>

                {expanded && (
                    <div className={cn(
                        isPassed ? "bg-success/[0.04] dark:bg-success/[0.08]" : "bg-failure/[0.04] dark:bg-failure/[0.08]",
                        "p-4 pt-2 border-t border-border/40")}>
                        {invocation.parameters.length > 0 && summary.kind !== 'inline' && (
                            <InvocationParameters parameters={invocation.parameters}/>
                        )}
                        <SectionRenderer
                            invocation={invocation}
                            testState={invocation.state}
                            autoOpenTab={autoOpenTab}
                            parseErrors={parseErrors}
                            testClass={testClass}
                        />
                    </div>
                )}
            </div>
        );
    }
);

InvocationCard.displayName = "InvocationCard";
