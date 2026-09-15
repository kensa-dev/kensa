import {Link} from "react-router-dom";
import {Badge} from "@/components/ui/badge";
import {Popover, PopoverAnchor, PopoverContent} from "@/components/ui/popover";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import {ConfigContext} from "@/contexts/ConfigContext";
import {useTagFilter} from "@/contexts/TagFilterContext";
import {type MouseEvent, useContext, useState} from "react";
import {Play} from "lucide-react";
import {cn} from "@/lib/utils";
import {TestState} from "@/types/Test";
import {issueHref} from "@/util/issueTrackerLink";
import {issueBadgeMenu} from "@/util/replayLink";
import {badgeFilterMenu} from "@/util/badgeFilterMenu";

interface IssueBadgeProps {
    issue: string;
    kind?: "issue" | "epic";
    testState: TestState;
}

export const IssueBadge = ({issue, kind = "issue", testState}: IssueBadgeProps) => {
    const {issueTrackerUrl, replayUrl} = useContext(ConfigContext);
    const {onFilterClick, query} = useTagFilter();
    const [menuOpen, setMenuOpen] = useState(false);

    const baseClasses = "rounded-md border transition-colors bg-clip-padding";

    const toneClasses = cn(
        testState === "Passed"
            ? [
                "border-success/30 text-success dark:text-success",
                "!bg-success/10 hover:!bg-success/15",
                "dark:!bg-success/10 dark:hover:!bg-success/15",
            ]
            : testState === "Failed"
                ? [
                    "border-failure/30 text-failure dark:text-failure",
                    "!bg-failure/10 hover:!bg-failure/15",
                    "dark:!bg-failure/10 dark:hover:!bg-failure/15",
                ]
                : [
                    "border-border/50 text-muted-foreground",
                    "!bg-muted/15 hover:!bg-muted/22",
                    "dark:!bg-muted/20 dark:hover:!bg-muted/30",
                ]
    );

    const href = issueHref(issueTrackerUrl, issue);
    const hasReplay = !!replayUrl?.trim();

    const openMenu = (e: MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setMenuOpen(true);
    };

    const glyph = hasReplay
        ? <Play className="!size-2.5 shrink-0 opacity-60 transition-opacity group-hover:opacity-100"/>
        : null;

    const badge = href
        ? (
            <Badge asChild className={cn(baseClasses, toneClasses, "group")}>
                <Link
                    target="_blank"
                    to={href}
                    onClick={(e) => e.stopPropagation()}
                    onMouseDown={(e) => e.stopPropagation()}
                    onContextMenu={openMenu}
                >
                    {issue}
                    {glyph}
                </Link>
            </Badge>
        )
        : (
            <Badge className={cn(baseClasses, toneClasses, "group")} onContextMenu={openMenu}>
                {issue}
                {glyph}
            </Badge>
        );

    const linkEntries = issueBadgeMenu(issueTrackerUrl, replayUrl, issue, kind);
    const filterEntries = badgeFilterMenu(query, kind, issue);
    const entryClasses = "rounded-md px-2 py-1.5 text-xs text-foreground hover:bg-muted/60 whitespace-nowrap";

    return (
        <Popover open={menuOpen} onOpenChange={setMenuOpen}>
            <Tooltip delayDuration={200}>
                <TooltipTrigger asChild>
                    <PopoverAnchor asChild>
                        {badge}
                    </PopoverAnchor>
                </TooltipTrigger>
                <TooltipContent className="bg-slate-900 text-white border-none shadow-xl text-xs px-3 py-2">
                    {hasReplay ? "Right-click for Replay" : "Right-click to filter"}
                </TooltipContent>
            </Tooltip>

            <PopoverContent
                align="start"
                sideOffset={6}
                className="w-auto min-w-[10rem] p-1 rounded-lg shadow-xl border-border/40"
                onClick={(e) => e.stopPropagation()}
                onMouseDown={(e) => e.stopPropagation()}
            >
                <div className="flex flex-col">
                    {linkEntries.map((entry) => (
                        <a
                            key={entry.key}
                            href={entry.href}
                            target="_blank"
                            rel="noreferrer"
                            className={entryClasses}
                            onClick={(e) => {
                                e.stopPropagation();
                                setMenuOpen(false);
                            }}
                            onMouseDown={(e) => e.stopPropagation()}
                        >
                            {entry.label}
                        </a>
                    ))}
                    {linkEntries.length > 0 && <div className="my-1 h-px bg-border/40"/>}
                    {filterEntries.map((entry) => (
                        <button
                            key={entry.key}
                            type="button"
                            className={cn(entryClasses, "text-left")}
                            onClick={(e) => {
                                e.stopPropagation();
                                setMenuOpen(false);
                                onFilterClick(kind, issue, entry.additive);
                            }}
                            onMouseDown={(e) => e.stopPropagation()}
                        >
                            {entry.label}
                        </button>
                    ))}
                </div>
            </PopoverContent>
        </Popover>
    );
};
