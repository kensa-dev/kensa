import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {IssueBadge} from './IssueBadge';
import {cn} from "@/lib/utils";

interface IssueListProps {
    issues?: string[];
    limit?: number;
}

export const IssueList = ({issues = [], limit = 5}: IssueListProps) => {
    if (!issues || issues.length === 0) return null;

    const hasMore = issues.length > limit;
    const initialIssues = issues.slice(0, limit);

    return (
        <div className="flex items-center gap-1 shrink-0">
            <div className="flex items-center gap-1">
                {initialIssues.map((issue) => (
                    <IssueBadge key={issue} issue={issue}/>
                ))}
            </div>

            {hasMore && (
                <Popover>
                    <PopoverTrigger asChild>
                        <button
                            type="button"
                            className={cn(
                                "flex items-center justify-center rounded-full bg-secondary/50 hover:bg-secondary transition-colors",
                                "text-[10px] h-5 px-2 font-bold border border-border/50 text-muted-foreground whitespace-nowrap outline-none"
                            )}
                        >
                            +{issues.length - limit} more
                        </button>
                    </PopoverTrigger>
                    <PopoverContent
                        className="w-80 p-3 shadow-xl border-border/40 bg-background/95 backdrop-blur-md rounded-xl"
                        align="end"
                        side="bottom"
                        sideOffset={10}
                    >
                        <div className="space-y-3">
                            <p className="text-[10px] font-black uppercase tracking-widest text-muted-foreground/60 px-0.5">
                                All Related Issues ({issues.length})
                            </p>

                            <div className="grid grid-cols-[repeat(auto-fill,minmax(100px,1fr))] gap-2 max-h-[300px] overflow-y-auto pr-1">
                                {issues.map((issue) => (
                                    <div key={issue} className="flex">
                                        <IssueBadge issue={issue}/>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </PopoverContent>
                </Popover>
            )}
        </div>
    );
};