import {describe, it, expect, vi, afterEach} from 'vitest';
import {loadTreeData, loadSearchIndexes, statusFor} from './initialLoad';
import type {Manifest} from '@/types/Manifest';
import type {RawSearchIndex} from '@/types/SearchIndex';

const manifestFor = (...ids: string[]): Manifest => ({
    schemaVersion: 1,
    sources: ids.map(id => ({id, title: `${id} title`, url: `sources/${id}`})),
});

const json = (body: unknown) => new Response(JSON.stringify(body), {status: 200});
const notFound = () => new Response('not found', {status: 404});

const config = {titleText: 'Configured Title', issueTrackerUrl: '', acronyms: {}, packageDisplay: 'Shortened', sectionOrder: [], alwaysExpandNotes: false};
const suiteIndex = {id: 'com.example.FooTest', testClass: 'com.example.FooTest', displayName: 'Foo Test', state: 'Passed'};

const fetchByPath = (routes: Record<string, () => Response | Promise<Response>>) =>
    vi.fn(async (url: RequestInfo | URL) => {
        const key = Object.keys(routes).find(k => String(url).includes(k));
        if (!key) return notFound();
        return routes[key]();
    }) as unknown as typeof fetch;

afterEach(() => {
    vi.unstubAllGlobals();
});

describe('loadTreeData', () => {
    it('builds a tagged root with a System View child when an aggregate diagram is present', async () => {
        vi.stubGlobal('fetch', fetchByPath({
            'configuration.json': () => json(config),
            'indices.json': () => json({indices: [suiteIndex], aggregateComponentDiagram: '<svg/>'}),
        }));

        const result = await loadTreeData(manifestFor('projA'));

        expect(result.failedSourceIds).toEqual([]);
        expect(result.roots).toHaveLength(1);
        const root = result.roots[0];
        expect(root.id).toBe('src:projA');
        expect(root.displayName).toBe('Configured Title');
        expect(root.children?.[0]).toMatchObject({type: 'system-view', sourceId: 'projA'});
        expect(root.children?.[1]).toMatchObject({id: 'projA::com.example.FooTest', sourceId: 'projA'});
        expect(result.diagramsBySource['projA']).toBe('<svg/>');
        expect(result.titles['projA']).toBe('Configured Title');
        expect(result.urls['projA']).toBe('sources/projA');
    });

    it('fetches configuration and indices in parallel', async () => {
        const requested: string[] = [];
        let releaseAll: () => void = () => {};
        const gate = new Promise<void>(r => { releaseAll = r; });

        vi.stubGlobal('fetch', vi.fn(async (url: RequestInfo | URL) => {
            requested.push(String(url));
            await gate;
            return String(url).includes('indices.json')
                ? json({indices: [suiteIndex]})
                : json(config);
        }) as unknown as typeof fetch);

        const pending = loadTreeData(manifestFor('projA'));
        await new Promise(r => setTimeout(r, 0));

        expect(requested.some(u => u.includes('configuration.json'))).toBe(true);
        expect(requested.some(u => u.includes('indices.json'))).toBe(true);

        releaseAll();
        await pending;
    });

    it('marks the source failed when indices cannot be loaded', async () => {
        vi.stubGlobal('fetch', fetchByPath({
            'configuration.json': () => json(config),
            'indices.json': () => notFound(),
        }));

        const result = await loadTreeData(manifestFor('projA'));

        expect(result.failedSourceIds).toEqual(['projA']);
        expect(result.roots).toEqual([]);
    });

    it('treats an empty indices list as loaded, not failed', async () => {
        vi.stubGlobal('fetch', fetchByPath({
            'configuration.json': () => json(config),
            'indices.json': () => json({indices: []}),
        }));

        const result = await loadTreeData(manifestFor('projA'));

        expect(result.failedSourceIds).toEqual([]);
        expect(result.roots).toEqual([]);
    });

    it('keeps successful sources when another source fails', async () => {
        vi.stubGlobal('fetch', vi.fn(async (url: RequestInfo | URL) => {
            const u = String(url);
            if (u.includes('sources/bad/')) return notFound();
            if (u.includes('indices.json')) return json({indices: [suiteIndex]});
            return json(config);
        }) as unknown as typeof fetch);

        const result = await loadTreeData(manifestFor('good', 'bad'));

        expect(result.failedSourceIds).toEqual(['bad']);
        expect(result.roots).toHaveLength(1);
        expect(result.roots[0].id).toBe('src:good');
    });

    it('falls back to the manifest title when configuration is unavailable', async () => {
        vi.stubGlobal('fetch', fetchByPath({
            'configuration.json': () => notFound(),
            'indices.json': () => json({indices: [suiteIndex]}),
        }));

        const result = await loadTreeData(manifestFor('projA'));

        expect(result.failedSourceIds).toEqual([]);
        expect(result.roots[0].displayName).toBe('projA title');
    });
});

describe('statusFor', () => {
    it('is error when every source failed', () => {
        expect(statusFor({failedSourceIds: ['a', 'b']}, manifestFor('a', 'b'))).toBe('error');
    });

    it('is ready when at least one source loaded', () => {
        expect(statusFor({failedSourceIds: ['b']}, manifestFor('a', 'b'))).toBe('ready');
    });

    it('is ready for a manifest with no sources', () => {
        expect(statusFor({failedSourceIds: []}, manifestFor())).toBe('ready');
    });
});

describe('loadSearchIndexes', () => {
    const searchIndex: RawSearchIndex = {
        schemaVersion: 1,
        terms: [{value: 'v-1', names: ['Fx'], locations: [{testId: 't', testMethod: 'm', invocation: 0, count: 1}]}],
    };

    it('merges per-source indexes with source tagging', async () => {
        vi.stubGlobal('fetch', fetchByPath({'search-index.json': () => json(searchIndex)}));

        const merged = await loadSearchIndexes(manifestFor('projA'));

        expect(merged.terms).toHaveLength(1);
        expect(merged.terms[0].locations[0]).toMatchObject({sourceId: 'projA'});
    });

    it('treats a failed search index as empty rather than fatal', async () => {
        vi.stubGlobal('fetch', fetchByPath({'search-index.json': () => notFound()}));

        const merged = await loadSearchIndexes(manifestFor('projA'));

        expect(merged.terms).toEqual([]);
    });
});
