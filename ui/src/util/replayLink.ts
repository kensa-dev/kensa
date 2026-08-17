import {KensaConfig} from "@/contexts/ConfigContext";
import {Manifest} from "@/types/Manifest";

export interface IssueMenuEntry {
    key: "issue" | "replay";
    label: string;
    href: string;
}

export const replayIssueHref = (replayUrl: string, issue: string): string => {
    const trimmed = replayUrl.trim().replace(/\/+$/, "");
    const base = trimmed.endsWith("/replay") ? trimmed : `${trimmed}/replay`;

    return `${base}/?tags=issue:${encodeURIComponent(issue)}`;
};

export const issueTrackerHref = (issueTrackerUrl: string, issue: string): string => {
    const trimmed = issueTrackerUrl.trim();

    return trimmed.endsWith("/") ? `${trimmed}${issue}` : `${trimmed}/${issue}`;
};

export const issueBadgeMenu = (issueTrackerUrl: string, replayUrl: string | undefined, issue: string): IssueMenuEntry[] => {
    const entries: IssueMenuEntry[] = [];

    if (issueTrackerUrl.trim()) entries.push({key: "issue", label: "Open issue", href: issueTrackerHref(issueTrackerUrl, issue)});
    if (replayUrl?.trim()) entries.push({key: "replay", label: "Open in Replay", href: replayIssueHref(replayUrl, issue)});

    return entries;
};

export const withReplayUrl = (config: KensaConfig, manifest: Manifest): KensaConfig => {
    const replayUrl = manifest.replayUrl?.trim();
    const rest = {...config};
    delete rest.replayUrl;

    return replayUrl ? {...rest, replayUrl} : rest;
};
