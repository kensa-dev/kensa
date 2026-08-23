import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";
import {FailuresModel} from "@/lib/overview";
import {PANEL_TITLE_CLASS, simpleName} from "./chartConfig";

const MAX_ROWS = 8;

const TAG_BUTTON_CLASS = "rounded border border-border px-1 font-mono text-[10px] text-muted-foreground hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

interface FailuresPanelProps {
    model: FailuresModel;
    onFilter: (term: string) => void;
    onNavigate: (classId: string, testMethod: string) => void;
}

export function FailuresPanel({model, onFilter, onNavigate}: FailuresPanelProps) {
    const shown = model.rows.slice(0, MAX_ROWS);
    const hidden = model.rows.length - shown.length;

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Failures</CardTitle>
            </CardHeader>
            <CardContent>
                <Table className="table-fixed">
                    <TableBody>
                        {shown.map(row => (
                            <TableRow key={row.nodeId}>
                                <TableCell className="max-w-0 px-0 py-1 text-xs">
                                    <span className="flex items-center gap-1.5">
                                        <span className="text-failure" aria-hidden="true">&#9679;</span>
                                        <button
                                            type="button"
                                            onClick={() => onNavigate(row.classId, row.testMethod)}
                                            title={`${row.testClass} · ${row.displayName}`}
                                            className="min-w-0 truncate rounded text-left hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                                        >
                                            <span className="text-muted-foreground">{simpleName(row.testClass)}</span>
                                            <span className="text-muted-foreground"> · </span>
                                            <span>{row.displayName}</span>
                                        </button>
                                    </span>
                                </TableCell>
                                <TableCell className="w-[38%] px-0 py-1 text-right">
                                    <span className="inline-flex flex-wrap justify-end gap-1">
                                        {row.epics.map(epic => (
                                            <button key={epic} type="button" onClick={() => onFilter(`epic:${epic}`)} className={TAG_BUTTON_CLASS}>{epic}</button>
                                        ))}
                                        {row.issues.map(issue => (
                                            <button key={issue} type="button" onClick={() => onFilter(`issue:${issue}`)} className={TAG_BUTTON_CLASS}>{issue}</button>
                                        ))}
                                    </span>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                {(hidden > 0 || model.concentration) && <p className="pt-2 text-[10px] text-muted-foreground">
                    {hidden > 0 && <span>+{hidden} more</span>}
                    {hidden > 0 && model.concentration && <span> · </span>}
                    {model.concentration && (
                        <span>
                            concentrated in{' '}
                            <button
                                type="button"
                                onClick={() => onFilter(`pkg:${model.concentration?.pkg}`)}
                                className="rounded font-mono hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                            >
                                {model.concentration.pkg}
                            </button>
                            {' '}({model.concentration.count} of {model.rows.length})
                        </span>
                    )}
                </p>}
            </CardContent>
        </Card>
    );
}
