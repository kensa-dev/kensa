import * as React from 'react';
import { Section } from '@/constants';

export interface KensaConfig {
    titleText: string;
    issueTrackerUrl: string;
    acronyms: Record<string, string>;
    packageDisplayMode: string;
    sectionOrder: string[];
    alwaysExpandNotes: boolean
}

// Default fallback configuration
export const DEFAULT_CONFIG: KensaConfig = {
    titleText: "Kensa",
    issueTrackerUrl: "",
    acronyms: {},
    packageDisplayMode: "ShowFullPackage",
    sectionOrder: [Section.Tabs, Section.Sentences, Section.Exception],
    alwaysExpandNotes: false
};

export const ConfigContext = React.createContext<KensaConfig>(DEFAULT_CONFIG);

export const useConfig = () => React.useContext(ConfigContext);