import {Badge} from "@/components/ui/badge";
import {cn} from "@/lib/utils";
import {useTagFilter} from "@/contexts/TagFilterContext";
import * as React from "react";

interface TagBadgeProps {
    tag: string;
    className?: string;
}

export const TagBadge = ({tag, className}: TagBadgeProps) => {
    const {onTagClick, selectedTags} = useTagFilter();
    const selected = selectedTags.has(tag);

    const baseClasses = "rounded-md border transition-colors";
    const unselectedTone = "bg-transparent border-neutral-400/60 text-neutral-700 hover:!bg-neutral-500/10 dark:border-neutral-500/50 dark:text-neutral-300 dark:hover:!bg-neutral-400/10";
    const selectedTone = "bg-primary/15 border-primary/60 text-primary font-semibold hover:!bg-primary/20 dark:bg-primary/25 dark:border-primary/70 dark:text-primary-foreground";

    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        const additive = e.metaKey || e.ctrlKey || e.shiftKey;
        onTagClick(tag, additive);
    };

    return (
        <Badge
            role="button"
            aria-pressed={selected}
            tabIndex={0}
            onClick={handleClick}
            className={cn(baseClasses, selected ? selectedTone : unselectedTone, "cursor-pointer select-none", className)}
        >
            {tag}
        </Badge>
    );
};
