import {afterEach, describe, expect, it, vi} from 'vitest';
import {loadManifestOrFallback} from './manifestLoader';
import type {Manifest} from '@/types/Manifest';

describe('loadManifestOrFallback', () => {
    const realFetch = global.fetch;

    afterEach(() => {
        global.fetch = realFetch;
    });

    it('returns the parsed manifest when manifest.json is found', async () => {
        const manifest: Manifest = {
            schemaVersion: 1,
            sources: [
                {id: 'uiTest', title: 'UI Tests', url: 'sources/uiTest'},
                {id: 'test', title: 'Acceptance Tests', url: 'sources/test'},
            ],
        };
        global.fetch = vi.fn(async () => new Response(JSON.stringify(manifest), {status: 200})) as unknown as typeof fetch;

        const result = await loadManifestOrFallback();

        expect(result.sources).toHaveLength(2);
        expect(result.sources[0].id).toBe('uiTest');
    });

    it('returns a synthetic single-source manifest when manifest.json is 404', async () => {
        global.fetch = vi.fn(async () => new Response('not found', {status: 404})) as unknown as typeof fetch;

        const result = await loadManifestOrFallback();

        expect(result.sources).toHaveLength(1);
        expect(result.sources[0].id).toBe('default');
        expect(result.sources[0].url).toBe('.');
    });

    it('returns a synthetic single-source manifest on network error', async () => {
        global.fetch = vi.fn(async () => { throw new Error('network down'); }) as unknown as typeof fetch;

        const result = await loadManifestOrFallback();

        expect(result.sources).toHaveLength(1);
        expect(result.sources[0].id).toBe('default');
    });

    it('requests manifest with no-store cache so stale browser caches do not lock viewers to a previous build', async () => {
        const fetchMock = vi.fn(async () => new Response(JSON.stringify({schemaVersion: 1, sources: []}), {status: 200}));
        global.fetch = fetchMock as unknown as typeof fetch;

        await loadManifestOrFallback();

        expect(fetchMock).toHaveBeenCalledWith('./manifest.json', expect.objectContaining({cache: 'no-store'}));
    });
});
