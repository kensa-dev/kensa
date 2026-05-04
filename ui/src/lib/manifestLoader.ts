import type {Manifest} from '@/types/Manifest';

const FALLBACK_MANIFEST: Manifest = {
    schemaVersion: 1,
    sources: [
        {id: 'default', title: 'Kensa Tests', url: '.'},
    ],
};

export async function loadManifestOrFallback(): Promise<Manifest> {
    try {
        const res = await fetch('./manifest.json', {cache: 'no-store'});
        if (!res.ok) return FALLBACK_MANIFEST;
        return (await res.json()) as Manifest;
    } catch {
        return FALLBACK_MANIFEST;
    }
}
