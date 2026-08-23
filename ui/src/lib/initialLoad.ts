import {loadJson} from "@/lib/utils";
import {mergeIndexes, type MergedIndex} from "@/lib/suiteSearch";
import type {KensaConfig} from "@/contexts/ConfigContext";
import type {Index, Indices} from "@/types/Index";
import type {Manifest} from "@/types/Manifest";
import type {RawSearchIndex} from "@/types/SearchIndex";

export interface TreeData {
    roots: Indices;
    diagramsBySource: Record<string, string>;
    sourceConfigsMap: Record<string, KensaConfig>;
    urls: Record<string, string>;
    titles: Record<string, string>;
    failedSourceIds: string[];
}

const tagWithSourceId = (nodes: Indices, sourceId: string): Indices =>
    nodes.map(n => ({
        ...n,
        id: n.testClass ? `${sourceId}::${n.id}` : n.id,
        sourceId,
        children: n.children ? tagWithSourceId(n.children, sourceId) : undefined,
    }));

export async function loadTreeData(manifest: Manifest): Promise<TreeData> {
    const perSource = await Promise.all(manifest.sources.map(async (source) => {
        const [config, indicesData] = await Promise.all([
            loadJson<KensaConfig>('configuration.json', `Configuration (${source.id})`, {baseUrl: source.url}),
            loadJson<{ indices: Indices; aggregateComponentDiagram?: string }>('indices.json', `Indices tree (${source.id})`, {baseUrl: source.url}),
        ]);
        return {source, config, indicesData};
    }));

    const sourceConfigsMap: Record<string, KensaConfig> = {};
    const roots: Indices = [];
    const diagramsBySource: Record<string, string> = {};
    const failedSourceIds: string[] = [];
    for (const {source, config, indicesData} of perSource) {
        if (config) sourceConfigsMap[source.id] = config;
        // A genuinely empty report still serves indices.json with an empty list;
        // a missing/unreachable one is a failed source.
        if (indicesData === null) {
            failedSourceIds.push(source.id);
            continue;
        }
        const sourceIndices = indicesData.indices ?? [];
        const sourceDiagram = indicesData.aggregateComponentDiagram;
        if (sourceDiagram) diagramsBySource[source.id] = sourceDiagram;
        if (sourceIndices.length === 0) continue;
        const tagged = tagWithSourceId(sourceIndices, source.id);
        // Each source root's first children are synthetic "Overview" and "System View"
        // entries — Overview summarises the source's run, System View points at that
        // source's own aggregate component diagram — keeping per-source architectures
        // separate in the sidebar tree rather than conflating them under one global view.
        const overviewNode: Index = {
            id: `overview:${source.id}`,
            type: 'overview',
            displayName: 'Overview',
            testClass: '',
            state: 'Passed',
            sourceId: source.id,
        };
        const children: Indices = sourceDiagram
            ? [
                overviewNode,
                {
                    id: `sysview:${source.id}`,
                    type: 'system-view',
                    displayName: 'System View',
                    testClass: '',
                    state: 'Passed',
                    sourceId: source.id,
                },
                ...tagged,
            ]
            : [overviewNode, ...tagged];
        roots.push({
            id: `src:${source.id}`,
            type: 'project',
            displayName: config?.titleText || source.title || source.id,
            testClass: '',
            state: tagged.some(i => i.state === 'Failed') ? 'Failed' : 'Passed',
            children,
            sourceId: source.id,
        });
    }

    const urls: Record<string, string> = {};
    const titles: Record<string, string> = {};
    for (const source of manifest.sources) {
        urls[source.id] = source.url;
        titles[source.id] = sourceConfigsMap[source.id]?.titleText || source.title || source.id;
    }

    return {roots, diagramsBySource, sourceConfigsMap, urls, titles, failedSourceIds};
}

export type LoadStatus = 'loading' | 'ready' | 'error';

export function statusFor(treeData: Pick<TreeData, 'failedSourceIds'>, manifest: Manifest): LoadStatus {
    const allFailed = manifest.sources.length > 0 && treeData.failedSourceIds.length === manifest.sources.length;
    return allFailed ? 'error' : 'ready';
}

export async function loadSearchIndexes(manifest: Manifest): Promise<MergedIndex> {
    const perSourceSearch = await Promise.all(manifest.sources.map(async (source) => ({
        sourceId: source.id,
        index: await loadJson<RawSearchIndex>('search-index.json', `Search index (${source.id})`, {baseUrl: source.url})
            ?? {schemaVersion: 1, terms: []},
    })));
    return mergeIndexes(perSourceSearch);
}
