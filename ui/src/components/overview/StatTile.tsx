import {cn} from "@/lib/utils";

interface StatTileProps {
    label: string;
    value: string;
    tone?: 'default' | 'success' | 'failure';
}

export function StatTile({label, value, tone = 'default'}: StatTileProps) {
    return (
        <div className="flex flex-col gap-0.5">
            <span className={cn("text-2xl font-bold font-mono leading-none", tone === 'success' && "text-success", tone === 'failure' && "text-failure")}>{value}</span>
            <span className="text-[10px] text-muted-foreground">{label}</span>
        </div>
    );
}
