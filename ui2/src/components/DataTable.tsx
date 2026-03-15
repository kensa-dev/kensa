import {NameAndValues, TestState} from "@/types/Test";
import {cn} from "@/lib/utils";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

const rowHover: Record<TestState, string> = {
    Passed: "hover:bg-success/10",
    Failed: "hover:bg-failure/10",
    Disabled: "hover:bg-disabled/10",
};

interface DataTableProps {
    data: NameAndValues;
    testState: TestState;
}

export const DataTable = ({data, testState}: DataTableProps) => (
    <Table className="text-[14px] table-fixed">
        <colgroup>
            <col className="w-1/3" />
            <col className="w-2/3" />
        </colgroup>

        <TableHeader>
            <TableRow className="border-b border-border hover:bg-transparent">
                <TableHead className="h-8 py-2 text-[10px] uppercase tracking-widest">Name</TableHead>
                <TableHead className="h-8 py-2 text-[10px] uppercase tracking-widest">Value</TableHead>
            </TableRow>
        </TableHeader>

        <TableBody className="divide-y divide-border/40 [&_tr:last-child]:border-b-0">
            {data.map((row, i) => (
                Object.entries(row).map(([key, val], j) => (
                    <TableRow key={`${i}-${j}`} className={cn("group transition-colors border-0", rowHover[testState])}>
                        <TableCell className="py-2 px-4 align-top truncate">{key}</TableCell>
                        <TableCell className="py-2 px-4 whitespace-pre-wrap break-words">{val}</TableCell>
                    </TableRow>
                ))
            ))}
        </TableBody>
    </Table>
);
