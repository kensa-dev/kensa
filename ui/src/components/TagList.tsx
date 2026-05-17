import {Popover, PopoverContent, PopoverTrigger} from "@/components/ui/popover";
import {TagBadge} from './TagBadge';
import {cn} from "@/lib/utils";

interface TagListProps {
    tags?: string[];
    limit?: number;
}

export const TagList = ({tags = [], limit = 5}: TagListProps) => {
    if (!tags || tags.length === 0) return null;

    const hasMore = tags.length > limit;
    const initialTags = tags.slice(0, limit);

    const moreBadgeClasses = "rounded-md border bg-transparent transition-colors border-neutral-400/60 text-neutral-700 hover:!bg-neutral-500/10 dark:border-neutral-500/50 dark:text-neutral-300 dark:hover:!bg-neutral-400/10";

    return (
        <div className="flex items-center gap-1 shrink-0">
            <div className="flex items-center gap-1">
                {initialTags.map((tag) => (
                    <TagBadge key={tag} tag={tag}/>
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
                            +{tags.length - limit} more
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
                                All Tags ({tags.length})
                            </p>

                            <div className="grid grid-cols-[repeat(auto-fill,minmax(100px,1fr))] gap-2 max-h-[300px] overflow-y-auto pr-1">
                                {tags.map((tag) => (
                                    <div key={tag} className="flex">
                                        <TagBadge tag={tag}/>
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
