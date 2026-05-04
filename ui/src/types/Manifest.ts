export interface SourceEntry {
    id: string;
    title: string;
    url: string;
}

export interface Manifest {
    schemaVersion: number;
    kensaVersion?: string;
    generatedAt?: string;
    sources: SourceEntry[];
}
