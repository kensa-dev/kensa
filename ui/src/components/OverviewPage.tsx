import {useEffect, useMemo, useRef, useState} from "react";
import {Indices} from "@/types/Index";
import {appendTerm, parseQuery} from "@/util/queryMeta";
import {filterIndices} from "@/utils/filterIndices";
import {buildOverview} from "@/lib/overview";
import {collectIntervals, computeTiming} from "@/lib/overviewTiming";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {ResultsPanel} from "@/components/overview/ResultsPanel";
import {StackedBarPanel} from "@/components/overview/StackedBarPanel";
import {FailuresPanel} from "@/components/overview/FailuresPanel";
import {EpicsPanel} from "@/components/overview/EpicsPanel";
import {DurationsPanel} from "@/components/overview/DurationsPanel";
import {SlowestPanel} from "@/components/overview/SlowestPanel";
import {ParticipantsPanel} from "@/components/overview/ParticipantsPanel";
import {DensityPanel} from "@/components/overview/DensityPanel";
import {TimingPanel} from "@/components/overview/TimingPanel";

export interface OverviewSource {
    id: string;
    title: string;
}

const HIGHLIGHT_MS = 800;

function isFullyInViewport(element: HTMLElement): boolean {
    const rect = element.getBoundingClientRect();
    return rect.top >= 0 && rect.bottom <= window.innerHeight;
}

interface OverviewPageProps {
    sourceId: string;
    indices: Indices;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    packageDepth: number;
    onNavigate: (classId: string, testMethod: string) => void;
    sources: OverviewSource[];
    onSourceChange: (sourceId: string) => void;
}

export function OverviewPage({sourceId, indices, searchQuery, onSearchChange, packageDepth, onNavigate, sources, onSourceChange}: OverviewPageProps) {
    const sourceRoot = useMemo(() => indices.find(index => index.sourceId === sourceId), [indices, sourceId]);
    const filtered = useMemo(
        () => sourceRoot ? filterIndices([sourceRoot], parseQuery(searchQuery), '').filteredIndices : [],
        [sourceRoot, searchQuery],
    );
    const model = useMemo(() => buildOverview(filtered, packageDepth), [filtered, packageDepth]);
    const timing = useMemo(() => sourceRoot ? computeTiming(collectIntervals([sourceRoot])) : null, [sourceRoot]);
    const slowestRef = useRef<HTMLDivElement>(null);
    const [slowestHighlighted, setSlowestHighlighted] = useState(false);

    useEffect(() => {
        if (!slowestHighlighted) return;
        const timer = window.setTimeout(() => setSlowestHighlighted(false), HIGHLIGHT_MS);
        return () => window.clearTimeout(timer);
    }, [slowestHighlighted]);

    if (!sourceRoot) return null;

    const onFilter = (term: string) => onSearchChange(appendTerm(searchQuery, term));
    const onBucketClick = () => {
        const card = slowestRef.current;
        if (!card) return;
        if (!isFullyInViewport(card)) card.scrollIntoView({behavior: 'smooth', block: 'start'});
        setSlowestHighlighted(true);
    };

    return (
        <div className="p-6 lg:p-4">
            <div className="mx-auto max-w-[1400px] space-y-4">
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <h1 className="text-xl font-black text-foreground">Overview</h1>
                        <p className="mt-1 text-sm text-muted-foreground">{sourceRoot.displayName}{searchQuery ? ` · filtered: ${searchQuery}` : ''}</p>
                    </div>
                    {sources.length > 1 && (
                        <Select value={sourceId} onValueChange={onSourceChange}>
                            <SelectTrigger className="h-8 w-[220px] text-xs" aria-label="Source">
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                {sources.map(source => <SelectItem key={source.id} value={source.id} className="text-xs">{source.title}</SelectItem>)}
                            </SelectContent>
                        </Select>
                    )}
                </div>
                {timing && <TimingPanel timing={timing}/>}
                {model.results ? (
                    <div className="grid grid-cols-1 gap-4 lg:grid-cols-12">
                        <div className="lg:col-span-3"><ResultsPanel counts={model.results} onFilter={onFilter}/></div>
                        {model.byTag && <div className="lg:col-span-4"><StackedBarPanel title="Results by tag" groups={model.byTag} prefix="tag:" onFilter={onFilter}/></div>}
                        {model.byPackage && <div className="lg:col-span-5"><StackedBarPanel title="Results by package" groups={model.byPackage} prefix="pkg:" onFilter={onFilter}/></div>}
                        {model.failures && <div className="lg:col-span-6"><FailuresPanel model={model.failures} onFilter={onFilter} onNavigate={onNavigate}/></div>}
                        {model.byEpic && <div className="lg:col-span-6"><EpicsPanel groups={model.byEpic} onFilter={onFilter}/></div>}
                        {model.durations && <div className="lg:col-span-4"><DurationsPanel buckets={model.durations} onBucketClick={onBucketClick}/></div>}
                        {model.slowest && <div className="lg:col-span-4"><SlowestPanel ref={slowestRef} rows={model.slowest} onNavigate={onNavigate} highlighted={slowestHighlighted}/></div>}
                        {model.participants && <div className="lg:col-span-4"><ParticipantsPanel groups={model.participants}/></div>}
                        {model.density && <div className="lg:col-span-4"><DensityPanel model={model.density} onNavigate={onNavigate}/></div>}
                    </div>
                ) : (
                    <p className="text-sm text-muted-foreground">No tests match the current filter.</p>
                )}
            </div>
        </div>
    );
}
