import {Link} from "react-router-dom"
import {Badge} from "@/components/ui/badge"
import {ConfigContext} from "@/contexts/ConfigContext.tsx";
import {useContext} from "react";

export const IssueBadge = ({issue}: { issue: string }) => {
    const {issueTrackerUrl} = useContext(ConfigContext);

    if (!issueTrackerUrl) {
        return (
            <Badge variant="outline">{issue}</Badge>
        )
    } else {
        const href = issueTrackerUrl.endsWith('/') ? `${issueTrackerUrl}${issue}` : `${issueTrackerUrl}/${issue}`;
        return (
            <Badge variant="outline" asChild>
                <Link target="_blank" to={href}>{issue}</Link>
            </Badge>
        );
    }
};