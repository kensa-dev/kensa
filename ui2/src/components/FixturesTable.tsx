import {FixtureSpec, NameAndValues, TestState} from "@/types/Test";
import {cn, buildHighlightRegex} from "@/lib/utils";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

const rowHover: Record<TestState, string> = {
    Passed: "hover:bg-success/10",
    Failed: "hover:bg-failure/10",
    Disabled: "hover:bg-disabled/10",
    "Not Executed": "hover:bg-muted/10",
};

function groupByFamily(fixtures: NameAndValues, specs: FixtureSpec[]): NameAndValues {
    const fixtureMap = new Map(fixtures.map(row => [Object.keys(row)[0], row]));
    const specMap = new Map(specs.map(s => [s.key, s]));

    const childrenOf = new Map<string, string[]>();
    for (const spec of specs) {
        for (const parent of spec.parents) {
            if (!childrenOf.has(parent)) childrenOf.set(parent, []);
            childrenOf.get(parent)!.push(spec.key);
        }
    }

    const result: NameAndValues = [];
    const emitted = new Set<string>();

    const tryEmit = (key: string) => {
        if (emitted.has(key) || !fixtureMap.has(key)) return;
        const spec = specMap.get(key);
        if (spec?.parents.some(p => fixtureMap.has(p) && !emitted.has(p))) return;
        emitted.add(key);
        result.push(fixtureMap.get(key)!);
        for (const child of (childrenOf.get(key) ?? [])) tryEmit(child);
    };

    for (const [key] of fixtureMap) tryEmit(key);
    return result;
}

function getDepth(key: string, specs: FixtureSpec[], visited = new Set<string>()): number {
    if (visited.has(key)) return 0;
    const spec = specs.find(s => s.key === key);
    if (!spec || spec.parents.length === 0) return 0;
    visited.add(key);
    return 1 + Math.max(...spec.parents.map(p => getDepth(p, specs, new Set(visited))));
}

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
    const hasHierarchy = fixtureSpecs.some(s => s.parents.length > 0);
    const ordered = hasHierarchy ? groupByFamily(fixtures, fixtureSpecs) : fixtures;

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
                {ordered.map((row, i) => {
                    const [key, val] = Object.entries(row)[0];
                    const depth = hasHierarchy ? getDepth(key, fixtureSpecs) : 0;

                    return (
                        <TableRow key={i} className={cn("group transition-colors border-0", rowHover[testState])}>
                            <TableCell className="py-2 px-4 align-top truncate">
                                {depth > 0 ? (
                                    <span className="inline-flex items-center gap-1">
                                        <span
                                            className="text-muted-foreground/50 select-none shrink-0"
                                            style={{paddingLeft: `${(depth - 1) * 12}px`}}
                                        >↳</span>
                                        <span>{key}</span>
                                    </span>
                                ) : key}
                            </TableCell>
                            <TableCell className="py-2 px-4 whitespace-pre-wrap break-words">
                                <HighlightedValue text={val} highlights={highlights}/>
                            </TableCell>
                        </TableRow>
                    );
                })}
            </TableBody>
        </Table>
    );
};
