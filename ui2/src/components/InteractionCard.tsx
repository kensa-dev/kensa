import { useState } from 'react';
import { ChevronDown, ChevronRight, Activity } from 'lucide-react';
import { InteractionContent } from './InteractionContent'; // Import Content
import { cn } from "@/lib/utils";

export const InteractionCard = ({ interaction, isPassed, onExpand }: any) => {
    const [isExpanded, setIsExpanded] = useState(false);

    return (
        <div className={cn(
            "mb-2 border rounded-lg overflow-hidden bg-card shadow-sm transition-colors",
            isPassed ? "border-success-30" : "border-failure-30"
        )}>
            <div
                className={cn(
                    "flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors select-none",
                    isPassed ? "hover:bg-success-10" : "hover:bg-failure-10"
                )}
                onClick={() => setIsExpanded(!isExpanded)}
            >
                <Activity size={14} className={isPassed ? "text-success" : "text-failure"} />
                <span className={"text-[12px] font-semibold flex-1 text-neutral-800 dark:text-neutral-100"}>{interaction.name}</span>
                {isExpanded ? <ChevronDown size={16} className="text-muted-foreground" /> : <ChevronRight size={16} className="text-muted-foreground" />}
            </div>

            {isExpanded && (
                <div className="p-2 border-t border-border/40">
                    <InteractionContent
                        interaction={interaction}
                        isPassed={isPassed}
                        onExpand={() => onExpand(interaction)}
                    />
                </div>
            )}
        </div>
    );
};