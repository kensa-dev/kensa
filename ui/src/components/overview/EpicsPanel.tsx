import {useState} from "react";
import {ChevronDown, ChevronRight} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";
import {EpicGroup, StateCounts} from "@/lib/overview";
import {cn} from "@/lib/utils";
import {PANEL_TITLE_CLASS, RESULT_BAR_CLASSES, RESULT_KEYS, RESULT_TEXT_CLASSES, ResultKey} from "./chartConfig";

const SHORT_LABELS: Record<ResultKey, string> = {
    passed: 'pass',
    failed: 'fail',
    disabled: 'disabled',
    notExecuted: 'not run',
};

const ID_BUTTON_CLASS = "rounded font-mono hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring";

interface EpicsPanelProps {
    groups: EpicGroup[];
    onFilter: (term: string) => void;
}

function MiniBar({counts}: {counts: StateCounts}) {
    return (
        <span className="flex h-1.5 w-16 shrink-0 overflow-hidden rounded-sm bg-muted">
            {RESULT_KEYS.filter(key => counts[key] > 0).map(key => (
                <span key={key} className={RESULT_BAR_CLASSES[key]} style={{width: `${(counts[key] / counts.total) * 100}%`}}/>
            ))}
        </span>
    );
}

function Counts({counts}: {counts: StateCounts}) {
    const present = RESULT_KEYS.filter(key => counts[key] > 0);
    return (
        <span className="whitespace-nowrap text-[10px] text-muted-foreground">
            {present.map((key, index) => (
                <span key={key}>
                    {index > 0 && ' · '}
                    <span className={RESULT_TEXT_CLASSES[key]}>{counts[key]}</span> {SHORT_LABELS[key]}
                </span>
            ))}
        </span>
    );
}

export function EpicsPanel({groups, onFilter}: EpicsPanelProps) {
    const [expanded, setExpanded] = useState<string[]>([]);
    const issueCount = new Set(groups.flatMap(group => group.issues.map(issue => issue.issue))).size;

    const toggle = (epic: string) => setExpanded(current => current.includes(epic) ? current.filter(e => e !== epic) : [...current, epic]);

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Epics &amp; issues</CardTitle>
            </CardHeader>
            <CardContent>
                <Table>
                    <TableBody>
                        {groups.map(group => {
                            const isExpanded = expanded.includes(group.epic);
                            return [
                                <TableRow key={group.epic}>
                                    <TableCell className="px-0 py-1 text-xs">
                                        <span className="flex items-center gap-1">
                                            <button
                                                type="button"
                                                onClick={() => toggle(group.epic)}
                                                aria-expanded={isExpanded}
                                                aria-label={`${isExpanded ? 'Collapse' : 'Expand'} ${group.epic}`}
                                                className="rounded text-muted-foreground hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                                            >
                                                {isExpanded ? <ChevronDown size={12}/> : <ChevronRight size={12}/>}
                                            </button>
                                            <button type="button" onClick={() => onFilter(`epic:${group.epic}`)} className={cn(ID_BUTTON_CLASS, "font-bold")}>{group.epic}</button>
                                            <span className="text-[10px] text-muted-foreground">{group.total} tests</span>
                                        </span>
                                    </TableCell>
                                    <TableCell className="px-0 py-1">
                                        <span className="flex items-center justify-end gap-2">
                                            <MiniBar counts={group}/>
                                            <Counts counts={group}/>
                                        </span>
                                    </TableCell>
                                </TableRow>,
                                ...(isExpanded ? group.issues.map(issue => (
                                    <TableRow key={`${group.epic}/${issue.issue}`}>
                                        <TableCell className="px-0 py-0.5 pl-5 text-xs">
                                            <button type="button" onClick={() => onFilter(`issue:${issue.issue}`)} className={ID_BUTTON_CLASS}>{issue.issue}</button>
                                        </TableCell>
                                        <TableCell className="px-0 py-0.5">
                                            <span className="flex items-center justify-end gap-2">
                                                <MiniBar counts={issue}/>
                                                <Counts counts={issue}/>
                                            </span>
                                        </TableCell>
                                    </TableRow>
                                )) : []),
                                ...(isExpanded && group.epicOnly > 0 ? [
                                    <TableRow key={`${group.epic}/epic-only`}>
                                        <TableCell className="px-0 py-0.5 pl-5 text-[10px] text-muted-foreground" colSpan={2}>
                                            {group.epicOnly} tests on epic only
                                        </TableCell>
                                    </TableRow>,
                                ] : []),
                            ];
                        })}
                    </TableBody>
                </Table>
                <p className="pt-2 text-[10px] text-muted-foreground">{groups.length} epics · {issueCount} issues</p>
            </CardContent>
        </Card>
    );
}
