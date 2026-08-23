import {forwardRef} from "react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";
import {SlowRow} from "@/lib/overview";
import {cn} from "@/lib/utils";
import {formatMs} from "@/util/formatMs";
import {PANEL_TITLE_CLASS, simpleName} from "./chartConfig";

interface SlowestPanelProps {
    rows: SlowRow[];
    onNavigate: (classId: string, testMethod: string) => void;
    highlighted: boolean;
}

export const SlowestPanel = forwardRef<HTMLDivElement, SlowestPanelProps>(function SlowestPanel({rows, onNavigate, highlighted}, ref) {
    return (
        <Card className={cn("h-full transition-shadow", highlighted && "ring-2 ring-primary/40")} ref={ref}>
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Slowest tests</CardTitle>
            </CardHeader>
            <CardContent>
                <Table className="table-fixed">
                    <TableBody>
                        {rows.map(row => (
                            <TableRow key={row.nodeId}>
                                <TableCell className="max-w-0 px-0 py-1 text-xs">
                                    <button
                                        type="button"
                                        onClick={() => onNavigate(row.classId, row.testMethod)}
                                        title={`${row.testClass} · ${row.displayName}`}
                                        className="block w-full truncate rounded text-left hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                                    >
                                        <span className="text-muted-foreground">{simpleName(row.testClass)}</span>
                                        <span className="text-muted-foreground"> · </span>
                                        <span>{row.displayName}</span>
                                    </button>
                                </TableCell>
                                <TableCell className="w-[70px] px-0 py-1 text-right font-mono text-xs tabular-nums">{formatMs(row.elapsedMs)}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
    );
});
