import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {IssueBadge} from './IssueBadge';
import {cn} from "@/lib/utils";

type TestState = "Passed" | "Failed";

interface IssueListProps {
    issues?: string[];
    limit?: number;
    testState?: TestState;
}

export const IssueList = ({issues = [], limit = 5, testState}: IssueListProps) => {
    if (!issues || issues.length === 0) return null;

    const hasMore = issues.length > limit;
    const initialIssues = issues.slice(0, limit);

    const moreBadgeClasses = cn(
        "rounded-md border transition-colors bg-clip-padding",
        testState === "Passed"
            ? [
                "border-success-30 text-success dark:text-success",
                "!bg-success-10 hover:!bg-success-15",
                "dark:!bg-success-10 dark:hover:!bg-success-15",
            ]
            : testState === "Failed"
                ? [
                    "border-failure-30 text-failure dark:text-failure",
                    "!bg-failure-10 hover:!bg-failure-15",
                    "dark:!bg-failure-10 dark:hover:!bg-failure-15",
                ]
                : [
                    "border-border/50 text-muted-foreground",
                    "!bg-muted/15 hover:!bg-muted/22",
                    "dark:!bg-muted/20 dark:hover:!bg-muted/30",
                ]
    );

    return (
        <div className="flex items-center gap-1 shrink-0">
            <div className="flex items-center gap-1">
                {initialIssues.map((issue) => (
                    <IssueBadge key={issue} issue={issue} testState={testState}/>
                ))}
            </div>

            {hasMore && (
                <Popover>
                    <PopoverTrigger asChild>
                        <button
                            type="button"
                            className={cn(
                                moreBadgeClasses,
                                "inline-flex items-center justify-center",
                                "text-[10px] h-5 px-2 font-bold whitespace-nowrap",
                                "outline-none cursor-pointer select-none"
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
                                        <IssueBadge issue={issue} testState={testState}/>
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