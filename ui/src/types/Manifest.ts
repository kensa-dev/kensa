export interface SourceEntry {
    id: string;
    title: string;
    url: string;
}

export interface Manifest {
    schemaVersion: number;
    kensaVersion?: string;
    hubVersion?: string;
    generatedAt?: string;
    sources: SourceEntry[];
}
