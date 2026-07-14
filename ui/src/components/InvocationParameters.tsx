import {NameAndValue, NameAndValues} from "@/types/Test";
import {classifyParamValue} from "@/util/paramValueClassifier";
import {cn} from "@/lib/utils";
import {Table, TableBody, TableCell, TableRow} from "@/components/ui/table";

interface InvocationParametersProps {
    parameters: NameAndValues;
}

export const InvocationParameters = ({parameters}: InvocationParametersProps) => {
    if (parameters.length === 0) return null;

    return (
        <div className="max-h-[40vh] overflow-y-auto rounded-md border border-border/40 mb-3">
            <Table className="text-[14px] table-fixed">
                <colgroup>
                    <col className="w-1/3"/>
                    <col className="w-2/3"/>
                </colgroup>
                <TableBody className="divide-y divide-border/40 [&_tr:last-child]:border-b-0">
                    {parameters.map((row: NameAndValue, i: number) => {
                        const entries = Object.entries(row);
                        return entries.map(([name, value], j) => {
                            const classified = classifyParamValue(value);
                            return (
                                <TableRow key={`${i}-${j}`} className="border-0 hover:bg-muted/30 transition-colors">
                                    <TableCell className="py-2 px-4 align-top font-mono text-muted-foreground truncate" title={name}>
                                        {name}
                                    </TableCell>
                                    <TableCell className={cn("py-2 px-4 whitespace-pre-wrap break-words", classified.className)}>
                                        {classified.displayValue}
                                    </TableCell>
                                </TableRow>
                            );
                        });
                    })}
                </TableBody>
            </Table>
        </div>
    );
};
