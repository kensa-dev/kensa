import * as React from 'react';
import {Section} from '@/constants';

export interface KensaConfig {
    titleText: string;
    issueTrackerUrl: string | null;
    acronyms: Record<string, string>;
    packageDisplay: string;
    packageDisplayRoot?: string;
    sectionOrder: string[];
    alwaysExpandNotes: boolean;
    kensaVersion?: string;
    generatedAt?: string;
    replayUrl?: string;
}

// Default fallback configuration
export const DEFAULT_CONFIG: KensaConfig = {
    titleText: "Kensa",
    issueTrackerUrl: null,
    acronyms: {},
    packageDisplay: "HideCommonPackages",
    sectionOrder: [Section.Tabs, Section.Sentences, Section.Exception],
    alwaysExpandNotes: false
};

export const ConfigContext = React.createContext<KensaConfig>(DEFAULT_CONFIG);

export const useConfig = () => React.useContext(ConfigContext);