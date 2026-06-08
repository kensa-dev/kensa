export interface RawSearchLocation {
    testId: string;
    testMethod: string;
    invocation: number;
    displayName?: string;
    count: number;
}

export interface RawSearchTerm {
    value: string;
    names: string[];
    locations: RawSearchLocation[];
}

export interface RawSearchIndex {
    schemaVersion: number;
    terms: RawSearchTerm[];
}
