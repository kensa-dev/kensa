import {KensaConfig} from "@/contexts/ConfigContext";
import {Manifest} from "@/types/Manifest";

export const replayIssueHref = (replayUrl: string, issue: string): string => {
    const trimmed = replayUrl.trim().replace(/\/+$/, "");
    const base = trimmed.endsWith("/replay") ? trimmed : `${trimmed}/replay`;

    return `${base}/?tags=issue:${encodeURIComponent(issue)}`;
};

export const withReplayUrl = (config: KensaConfig, manifest: Manifest): KensaConfig => {
    const replayUrl = manifest.replayUrl?.trim();

    return replayUrl ? {...config, replayUrl} : {...config, replayUrl: undefined};
};
