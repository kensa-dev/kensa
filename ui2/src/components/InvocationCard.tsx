import * as React from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import { cn } from "@/lib/utils";
import { SectionRenderer } from './SectionRenderer';
import {Invocation, NameAndValue} from "@/types/Test.ts";

interface InvocationCardProps {
    invocation: Invocation;
    autoOpenTab?: string;
    isLast: boolean;
    initialExpanded?: boolean;
}

export const InvocationCard = ({ invocation, autoOpenTab, isLast, initialExpanded }: InvocationCardProps) => {
    const [isExpanded, setIsExpanded] = React.useState<boolean>(() => {
        if (initialExpanded !== undefined) return initialExpanded;
        return invocation.state === 'Failed';
    });

    React.useEffect(() => {
        if (initialExpanded !== undefined) {
            setIsExpanded(initialExpanded);
        }
    }, [initialExpanded]);

    const isPassed = invocation.state === 'Passed';
    const parametersTitle = React.useMemo(() => {
        if (!invocation.parameters || invocation.parameters.length === 0) {
            return invocation.displayName || "Invocation";
        }
        return invocation.parameters.map((p: NameAndValue) => {
            const [key, val] = Object.entries(p)[0];
            return `${key}: ${val}`;
        }).join(', ');
    }, [invocation]);

    return (
        <div className={cn(
            "border rounded-lg overflow-hidden transition-all duration-200 bg-background",
            !isLast && "mb-3",
            isPassed ? "border-success-30 shadow-sm" : "border-failure-30 shadow-sm"
        )}>
            <div
                className={cn(
                    "px-4 py-2 flex items-center justify-between cursor-pointer select-none transition-colors",
                    isPassed ? "hover:bg-success-10 text-neutral-800 dark:text-neutral-100" : "hover:bg-failure-10 text-neutral-800 dark:text-neutral-100"
                )}
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <div className="flex items-center gap-3 overflow-hidden">
                    {isExpanded ? <ChevronDown size={14} className="shrink-0 opacity-50" /> : <ChevronRight size={14} className="shrink-0 opacity-50" />}
                    <span className="text-[11px] font-mono truncate tracking-tight font-semibold">
                        {parametersTitle}
                    </span>
                </div>
                {invocation.elapsedTime && <span className="text-[10px] font-mono opacity-40">{invocation.elapsedTime}</span>}
            </div>

            {isExpanded && (
                <div className={cn(
                    isPassed ? "bg-success-2 dark:bg-success-10" : "bg-failure-2 dark:bg-success-10",
                    "p-4 pt-2 border-t border-border/40")}>
                    <SectionRenderer
                        invocation={invocation}
                        testState={invocation.state}
                        autoOpenTab={autoOpenTab}
                    />
                </div>
            )}
        </div>
    );
};