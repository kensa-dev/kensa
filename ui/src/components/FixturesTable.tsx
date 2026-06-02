import {FixtureSpec, NameAndValues, TestState} from "@/types/Test";
import {cn, buildHighlightRegex} from "@/lib/utils";
import {orderedFixtureRows} from "@/util/fixtureOrdering";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

const rowHover: Record<TestState, string> = {
    Passed: "hover:bg-success/10",
    Failed: "hover:bg-failure/10",
    Disabled: "hover:bg-disabled/10",
    "Not Executed": "hover:bg-muted/10",
};

interface HighlightedValueProps {
    text: string;
    highlights: string[];
}

const HighlightedValue = ({text, highlights}: HighlightedValueProps) => {
    if (highlights.length === 0) return <>{text}</>;
    const parts = text.split(buildHighlightRegex(highlights));
    return <>
        {parts.map((part, i) =>
            i % 2 === 1
                ? <span key={i} className="kensa-highlight">{part}</span>
                : part
        )}
    </>;
};

interface FixturesTableProps {
    fixtures: NameAndValues;
    fixtureSpecs: FixtureSpec[];
    testState: TestState;
    highlights?: string[];
}

export const FixturesTable = ({fixtures, fixtureSpecs, testState, highlights = []}: FixturesTableProps) => {
    const rows = orderedFixtureRows(fixtures, fixtureSpecs);

    return (
        <Table className="text-[14px] table-fixed">
            <colgroup>
                <col className="w-1/3"/>
                <col className="w-2/3"/>
            </colgroup>

            <TableHeader>
                <TableRow className="border-b border-border hover:bg-transparent">
                    <TableHead className="h-8 py-2 text-[10px] uppercase tracking-widest">Name</TableHead>
                    <TableHead className="h-8 py-2 text-[10px] uppercase tracking-widest">Value</TableHead>
                </TableRow>
            </TableHeader>

            <TableBody className="divide-y divide-border/40 [&_tr:last-child]:border-b-0">
                {rows.map((row, i) => (
                    <TableRow key={i} className={cn("group transition-colors border-0", rowHover[testState])}>
                        <TableCell className="py-2 px-4 align-top truncate">
                            {row.depth > 0 ? (
                                <span className="inline-flex items-center gap-1">
                                    <span
                                        className="text-muted-foreground/50 select-none shrink-0"
                                        style={{paddingLeft: `${(row.depth - 1) * 12}px`}}
                                    >↳</span>
                                    <span>{row.key}</span>
                                </span>
                            ) : row.key}
                        </TableCell>
                        <TableCell className="py-2 px-4 whitespace-pre-wrap break-words">
                            <HighlightedValue text={row.value} highlights={highlights}/>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
};
