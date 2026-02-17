import {Link} from "react-router-dom";
import {Badge} from "@/components/ui/badge";
import {ConfigContext} from "@/contexts/ConfigContext.tsx";
import {useContext} from "react";
import {cn} from "@/lib/utils";
import {TestState} from "@/types/Test.ts";

export const IssueBadge = ({issue, testState}: { issue: string; testState: TestState }) => {
    const {issueTrackerUrl} = useContext(ConfigContext);

    const baseClasses = "rounded-md border transition-colors bg-clip-padding";

    const toneClasses = cn(
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

    if (!issueTrackerUrl) {
        return (
            <Badge className={cn(baseClasses, toneClasses)}>
                {issue}
            </Badge>
        );
    }

    const href = issueTrackerUrl.endsWith("/")
        ? `${issueTrackerUrl}${issue}`
        : `${issueTrackerUrl}/${issue}`;

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
};