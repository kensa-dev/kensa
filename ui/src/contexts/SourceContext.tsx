import * as React from 'react';

export interface SourceContextValue {
    /** baseUrl for resolving per-source artifacts (configuration.json, indices.json, results/*, tabs/*). Defaults to '.' (single-bundle mode). */
    baseUrl: string;
}

export const SourceContext = React.createContext<SourceContextValue>({baseUrl: '.'});

export const useSource = () => React.useContext(SourceContext);
