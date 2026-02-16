import {NameAndValues} from "@/types/Test.ts";
import {cn} from "@/lib/utils.ts";

export const DataTable = ({data, isPassed}: { data: NameAndValues, isPassed: boolean }) => (
    <table className="w-full text-left border-collapse text-[13px] table-fixed">
        <colgroup>
            <col className="w-1/3" />
            <col className="w-2/3" />
        </colgroup>

        <thead>
        <tr className="border-b border-border text-[10px] uppercase tracking-widest text-muted-foreground">
            <th className="py-2 font-semibold">Name</th>
            <th className="py-2 font-semibold">Value</th>
        </tr>
        </thead>

        <tbody className="divide-y divide-border/40">
        {data.map((row, i) => (
            Object.entries(row).map(([key, val], j) => (
                <tr key={`${i}-${j}`} className={cn(
                    "group transition-colors",
                    isPassed ? "hover:bg-success-10" : "hover:bg-failure-10"
                )}>
                    <td className="py-2 align-top truncate">{key}</td>
                    <td className="py-2 whitespace-pre-wrap break-words">{val}</td>
                </tr>
            ))
        ))}
        </tbody>
    </table>
);