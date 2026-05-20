import * as React from 'react';
import {ChevronDown, ChevronRight} from 'lucide-react';
import {Invocation, NameAndValue, TestState} from "@/types/Test";
import {classifyParamValue} from "@/util/paramValueClassifier";
import {cn} from "@/lib/utils";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

const statePill: Record<TestState, string> = {
    Passed: "bg-success/10 text-success",
    Failed: "bg-failure/10 text-failure",
    Disabled: "bg-disabled/10 text-disabled",
    "Not Executed": "bg-muted/40 text-muted-foreground",
};

interface InvocationParameterMatrixProps {
    invocations: Invocation[];
    onInvocationSelect: (idx: number) => void;
}

const collectParamNames = (invocations: Invocation[]): string[] => {
    const seen = new Set<string>();
    const ordered: string[] = [];
    invocations.forEach((inv) => {
        inv.parameters.forEach((p: NameAndValue) => {
            Object.keys(p).forEach((k) => {
                if (!seen.has(k)) {
                    seen.add(k);
                    ordered.push(k);
                }
            });
        });
    });
    return ordered;
};

const lookupParam = (parameters: NameAndValue[], name: string): string | undefined => {
    for (const row of parameters) {
        if (Object.prototype.hasOwnProperty.call(row, name)) {
            return row[name];
        }
    }
    return undefined;
};

export const InvocationParameterMatrix = React.forwardRef<HTMLDivElement, InvocationParameterMatrixProps>(({invocations, onInvocationSelect}, ref) => {
    const [expanded, setExpanded] = React.useState(true);
    const paramNames = React.useMemo(() => collectParamNames(invocations), [invocations]);

    if (paramNames.length === 0) return null;

    return (
        <div ref={ref} className="rounded-md border border-border/40 mb-3 sticky top-0 z-20 bg-background">
            <button
                type="button"
                onClick={() => setExpanded(!expanded)}
                className="w-full px-3 py-1.5 flex items-center gap-2 text-[11px] tracking-tight text-muted-foreground hover:text-foreground transition-colors"
            >
                {expanded ? <ChevronDown size={12}/> : <ChevronRight size={12}/>}
                <span>Parameters across {invocations.length} invocations</span>
            </button>
            {expanded && (
                <div className="overflow-auto max-h-[40vh] border-t border-border/40">
                    <Table className="text-[12px]">
                        <TableHeader>
                            <TableRow className="border-b border-border/40 hover:bg-transparent">
                                <TableHead className="sticky left-0 bg-background px-4 py-2 font-medium text-muted-foreground/80 text-[11px] z-10 w-[1%] whitespace-nowrap">
                                    #
                                </TableHead>
                                {paramNames.map((name) => (
                                    <TableHead key={name} className="px-4 py-2 font-mono font-medium text-muted-foreground/80 text-[11px] whitespace-nowrap">
                                        {name}
                                    </TableHead>
                                ))}
                            </TableRow>
                        </TableHeader>
                        <TableBody className="divide-y divide-border/30 [&_tr:last-child]:border-b-0">
                            {invocations.map((inv, idx) => (
                                <TableRow
                                    key={idx}
                                    className="border-0 cursor-pointer hover:bg-muted/30 transition-colors"
                                    onClick={() => onInvocationSelect(idx)}
                                >
                                    <TableCell className="sticky left-0 bg-background px-4 py-2 z-10 whitespace-nowrap w-[1%]">
                                        <span className={cn(
                                            "inline-flex items-center gap-1.5 px-1.5 py-0.5 rounded text-[10px] font-mono font-bold",
                                            statePill[inv.state]
                                        )}>
                                            <span className="opacity-60">#{idx + 1}</span>
                                            <span>{inv.state}</span>
                                        </span>
                                    </TableCell>
                                    {paramNames.map((name) => {
                                        const raw = lookupParam(inv.parameters, name);
                                        if (raw === undefined) {
                                            return (
                                                <TableCell key={name} className="px-4 py-2 text-muted-foreground/40">—</TableCell>
                                            );
                                        }
                                        const classified = classifyParamValue(raw);
                                        return (
                                            <TableCell key={name} className={cn("px-4 py-2 whitespace-pre-wrap break-words max-w-xs align-top", classified.className)}>
                                                {classified.displayValue}
                                            </TableCell>
                                        );
                                    })}
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            )}
        </div>
    );
});

InvocationParameterMatrix.displayName = "InvocationParameterMatrix";
