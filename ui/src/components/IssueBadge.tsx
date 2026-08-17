import {Link} from "react-router-dom";
import {Badge} from "@/components/ui/badge";
import {Popover, PopoverAnchor, PopoverContent} from "@/components/ui/popover";
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip";
import {ConfigContext} from "@/contexts/ConfigContext";
import {type MouseEvent, useContext, useState} from "react";
import {Play} from "lucide-react";
import {cn} from "@/lib/utils";
import {TestState} from "@/types/Test";
import {issueHref} from "@/util/issueTrackerLink";
import {issueBadgeMenu} from "@/util/replayLink";

interface IssueBadgeProps {
    issue: string;
    testState: TestState;
}

export const IssueBadge = ({issue, testState}: IssueBadgeProps) => {
    const {issueTrackerUrl, replayUrl} = useContext(ConfigContext);
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

    if (!replayUrl?.trim()) {
        if (!href) {
            return (
                <Badge className={cn(baseClasses, toneClasses)}>
                    {issue}
                </Badge>
            );
        }

        return (
            <Badge asChild className={cn(baseClasses, toneClasses)}>
                <Link
                    target="_blank"
                    to={href}
                    onClick={(e) => e.stopPropagation()}
                    onMouseDown={(e) => e.stopPropagation()}
                >
                    {issue}
                </Link>
            </Badge>
        );
    }

    const openMenu = (e: MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        setMenuOpen(true);
    };

    const glyph = <Play className="ml-1 size-2.5 shrink-0 opacity-60 transition-opacity group-hover:opacity-100"/>;

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

    return (
        <Popover open={menuOpen} onOpenChange={setMenuOpen}>
            <Tooltip delayDuration={200}>
                <TooltipTrigger asChild>
                    <PopoverAnchor asChild>
                        {badge}
                    </PopoverAnchor>
                </TooltipTrigger>
                <TooltipContent className="bg-slate-900 text-white border-none shadow-xl text-xs px-3 py-2">
                    Right-click for Replay
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
                    {issueBadgeMenu(issueTrackerUrl, replayUrl, issue).map((entry) => (
                        <a
                            key={entry.key}
                            href={entry.href}
                            target="_blank"
                            rel="noreferrer"
                            className="rounded-md px-2 py-1.5 text-xs text-foreground hover:bg-muted/60 whitespace-nowrap"
                            onClick={(e) => {
                                e.stopPropagation();
                                setMenuOpen(false);
                            }}
                            onMouseDown={(e) => e.stopPropagation()}
                        >
                            {entry.label}
                        </a>
                    ))}
                </div>
            </PopoverContent>
        </Popover>
    );
};
