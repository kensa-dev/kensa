import { useEffect, useRef, useState } from 'react';
import { ChevronDown, ChevronRight, Search, Tag, X } from 'lucide-react';
import { summary } from '@/lib/suiteSearch';
import { useSuiteSearch, type SuiteSearchResult } from '@/contexts/SuiteSearchContext';
import { nodeIdForLocation } from '@/util/suiteSearchNav';
import { hasOpenDialog } from '@/util/escapeGuard';
import { cn } from '@/lib/utils';

const MIN_WIDTH = 280;
const MAX_WIDTH = 720;
const WIDTH_KEY = 'kensa-suite-panel-width';

interface MethodGroup {
    testMethod: string;
    locations: SuiteSearchResult[];
}

interface ClassGroup {
    testId: string;
    methods: MethodGroup[];
}

interface ProjectGroup {
    sourceId: string;
    classes: ClassGroup[];
}

function groupResults(results: SuiteSearchResult[]): ProjectGroup[] {
    const bySource = new Map<string, Map<string, Map<string, SuiteSearchResult[]>>>();
    for (const result of results) {
        let classes = bySource.get(result.sourceId);
        if (!classes) { classes = new Map(); bySource.set(result.sourceId, classes); }
        let methods = classes.get(result.testId);
        if (!methods) { methods = new Map(); classes.set(result.testId, methods); }
        let locations = methods.get(result.testMethod);
        if (!locations) { locations = []; methods.set(result.testMethod, locations); }
        locations.push(result);
    }
    return [...bySource.entries()].map(([sourceId, classes]) => ({
        sourceId,
        classes: [...classes.entries()]
            .sort((a, b) => a[0].localeCompare(b[0]))
            .map(([testId, methods]) => ({
                testId,
                methods: [...methods.entries()]
                    .sort((a, b) => a[0].localeCompare(b[0]))
                    .map(([testMethod, locs]) => ({
                        testMethod,
                        locations: [...locs].sort((a, b) => a.invocation - b.invocation),
                    })),
            })),
    }));
}

const sumCount = (locations: SuiteSearchResult[]) => locations.reduce((s, l) => s + l.count, 0);

interface SuiteSearchResultsProps {
    sourceTitles: Record<string, string>;
    onNavigate: (location: SuiteSearchResult) => void;
    selectedNodeId: string | null;
    selectedResultKey: string | null;
}

export const SuiteSearchResults = ({ sourceTitles, onNavigate, selectedNodeId, selectedResultKey }: SuiteSearchResultsProps) => {
    const { activeTerm, results, isPanelOpen, openPanel, closePanel, clear } = useSuiteSearch();

    const [panelWidth, setPanelWidth] = useState<number>(() => {
        const saved = Number(localStorage.getItem(WIDTH_KEY));
        return saved >= MIN_WIDTH && saved <= MAX_WIDTH ? saved : 360;
    });
    const widthRef = useRef(panelWidth);
    widthRef.current = panelWidth;

    const [expandedKeys, setExpandedKeys] = useState<Set<string>>(() => new Set());

    useEffect(() => {
        if (!isPanelOpen) return;
        const onKey = (e: KeyboardEvent) => {
            if (e.key !== 'Escape') return;
            if (hasOpenDialog()) return;
            e.preventDefault();
            e.stopPropagation();
            closePanel();
        };
        window.addEventListener('keydown', onKey, true);
        return () => window.removeEventListener('keydown', onKey, true);
    }, [isPanelOpen, closePanel]);

    if (!activeTerm) return null;

    const counts = summary(results);
    const projects = groupResults(results);
    const testsLabel = `${counts.tests} ${counts.tests === 1 ? 'test' : 'tests'}`;
    const Icon = activeTerm.kind === 'value' ? Search : Tag;

    if (!isPanelOpen) {
        return (
            <button
                type="button"
                onClick={openPanel}
                title={`Reopen suite search: ${activeTerm.query}`}
                className="fixed right-0 top-20 z-30 flex items-center gap-1.5 rounded-l-md border border-r-0 bg-background/95 py-2 pl-2.5 pr-2 text-xs font-medium shadow-md backdrop-blur hover:bg-accent"
            >
                <Icon className="h-3.5 w-3.5 shrink-0 text-violet-500" />
                <span className="max-w-[12rem] truncate font-mono text-violet-600 dark:text-violet-300">{activeTerm.query}</span>
                <span className="text-muted-foreground">· {testsLabel}</span>
            </button>
        );
    }

    const startDrag = (e: React.MouseEvent) => {
        e.preventDefault();
        const onMove = (ev: MouseEvent) => {
            const w = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, window.innerWidth - ev.clientX));
            widthRef.current = w;
            setPanelWidth(w);
        };
        const onUp = () => {
            window.removeEventListener('mousemove', onMove);
            window.removeEventListener('mouseup', onUp);
            document.body.style.userSelect = '';
            localStorage.setItem(WIDTH_KEY, String(widthRef.current));
        };
        document.body.style.userSelect = 'none';
        window.addEventListener('mousemove', onMove);
        window.addEventListener('mouseup', onUp);
    };

    const isActiveLocation = (loc: SuiteSearchResult) =>
        selectedResultKey !== null
        && nodeIdForLocation(loc) === selectedNodeId
        && `${nodeIdForLocation(loc)}|${loc.testMethod}|${loc.invocation}` === selectedResultKey;

    const toggle = (key: string) =>
        setExpandedKeys((prev) => {
            const next = new Set(prev);
            next.has(key) ? next.delete(key) : next.add(key);
            return next;
        });

    return (
        <aside style={{ width: panelWidth }} className="relative flex h-full shrink-0 flex-col border-l bg-background">
            <div
                onMouseDown={startDrag}
                title="Drag to resize"
                className="absolute left-0 top-0 z-10 -ml-0.5 h-full w-1.5 cursor-col-resize bg-transparent transition-colors hover:bg-primary/30"
            />

            <div className="flex items-start justify-between gap-2 border-b px-4 py-3">
                <div className="min-w-0">
                    <div className="flex items-center gap-2 text-sm font-semibold">
                        <Icon className="h-3.5 w-3.5 shrink-0 text-violet-500" />
                        <span className="truncate">{activeTerm.kind === 'value' ? 'Value' : 'Fixture'}</span>
                        <span className="truncate rounded bg-violet-500/10 px-1.5 py-0.5 font-mono text-xs text-violet-600 dark:text-violet-300">
                            {activeTerm.query}
                        </span>
                    </div>
                    <div className="mt-1 text-xs text-muted-foreground">
                        {results.length === 0
                            ? 'No matches across the suite'
                            : `used in ${testsLabel} · ${counts.projects} ${counts.projects === 1 ? 'project' : 'projects'}`}
                    </div>
                </div>
                <div className="flex shrink-0 items-center gap-0.5">
                    <button type="button" onClick={clear} className="rounded-sm px-1.5 py-1 text-[11px] text-muted-foreground hover:bg-accent hover:text-foreground">Clear</button>
                    <button type="button" onClick={closePanel} aria-label="Collapse suite search" className="rounded-sm p-1 opacity-70 hover:bg-accent hover:opacity-100">
                        <X className="h-4 w-4" />
                    </button>
                </div>
            </div>

            <div className="flex-1 overflow-y-auto p-2">
                {projects.map((project) => (
                    <div key={project.sourceId} className="mb-3">
                        <div className="px-2 py-1 text-[10px] font-bold uppercase tracking-widest text-muted-foreground">
                            {sourceTitles[project.sourceId] ?? project.sourceId}
                        </div>

                        <div className="space-y-0.5">
                            {project.classes.map((cls) => {
                                const lastDot = cls.testId.lastIndexOf('.');
                                const shortClass = lastDot >= 0 ? cls.testId.slice(lastDot + 1) : cls.testId;
                                const pkg = lastDot >= 0 ? cls.testId.slice(0, lastDot) : '';
                                const classKey = `c:${project.sourceId}::${cls.testId}`;
                                const classActive = cls.methods.some((m) => m.locations.some(isActiveLocation));
                                const classOpen = expandedKeys.has(classKey) || classActive;
                                const classHits = cls.methods.reduce((s, m) => s + sumCount(m.locations), 0);

                                return (
                                    <div key={classKey}>
                                        <button
                                            type="button"
                                            onClick={() => toggle(classKey)}
                                            className={cn(
                                                'flex w-full items-center gap-1.5 rounded-md border-l-2 px-2 py-1.5 text-left',
                                                classActive ? 'border-violet-500 bg-violet-500/[0.06]' : 'border-transparent hover:bg-accent',
                                            )}
                                        >
                                            {classOpen ? <ChevronDown className="h-3.5 w-3.5 shrink-0 text-muted-foreground" /> : <ChevronRight className="h-3.5 w-3.5 shrink-0 text-muted-foreground" />}
                                            <span className="flex min-w-0 flex-col">
                                                <span className="truncate text-sm font-semibold">{shortClass}</span>
                                                {pkg && <span className="truncate font-mono text-[10px] text-muted-foreground">{pkg}</span>}
                                            </span>
                                            <span className="ml-auto flex shrink-0 items-center gap-1.5 font-mono text-[10px] text-muted-foreground">
                                                <span className="rounded bg-muted px-1">{cls.methods.length} {cls.methods.length === 1 ? 'method' : 'methods'}</span>
                                                <span className="rounded bg-violet-500/10 px-1 text-violet-600 dark:text-violet-300">{classHits}×</span>
                                            </span>
                                        </button>

                                        {classOpen && (
                                            <div className="ml-3 mt-0.5 space-y-0.5 border-l border-border/40 pl-2">
                                                {cls.methods.map((method) => {
                                                    const methodKey = `m:${project.sourceId}::${cls.testId}|${method.testMethod}`;
                                                    const methodActive = method.locations.some(isActiveLocation);

                                                    if (method.locations.length === 1) {
                                                        const loc = method.locations[0];
                                                        const active = isActiveLocation(loc);
                                                        return (
                                                            <button
                                                                key={methodKey}
                                                                type="button"
                                                                onClick={() => onNavigate(loc)}
                                                                aria-current={active ? 'true' : undefined}
                                                                className={cn(
                                                                    'flex w-full items-center justify-between gap-2 rounded-md border-l-2 px-2 py-1 text-left',
                                                                    active ? 'border-violet-500 bg-violet-500/10' : 'border-transparent hover:bg-accent',
                                                                )}
                                                            >
                                                                <span className={cn('truncate text-[13px] font-medium', active && 'text-violet-700 dark:text-violet-300')}>{method.testMethod}</span>
                                                                <span className="shrink-0 rounded bg-violet-500/10 px-1 font-mono text-[10px] text-violet-600 dark:text-violet-300">{loc.count}×</span>
                                                            </button>
                                                        );
                                                    }

                                                    const methodOpen = expandedKeys.has(methodKey) || methodActive;
                                                    const methodHits = sumCount(method.locations);
                                                    return (
                                                        <div key={methodKey}>
                                                            <button
                                                                type="button"
                                                                onClick={() => toggle(methodKey)}
                                                                className={cn(
                                                                    'flex w-full items-center gap-1.5 rounded-md border-l-2 px-2 py-1 text-left',
                                                                    methodActive ? 'border-violet-500 bg-violet-500/[0.06]' : 'border-transparent hover:bg-accent',
                                                                )}
                                                            >
                                                                {methodOpen ? <ChevronDown className="h-3 w-3 shrink-0 text-muted-foreground" /> : <ChevronRight className="h-3 w-3 shrink-0 text-muted-foreground" />}
                                                                <span className={cn('truncate text-[13px] font-medium', methodActive && 'text-violet-700 dark:text-violet-300')}>{method.testMethod}</span>
                                                                <span className="ml-auto flex shrink-0 items-center gap-1.5 font-mono text-[10px] text-muted-foreground">
                                                                    <span className="rounded bg-muted px-1">{method.locations.length} runs</span>
                                                                    <span className="rounded bg-violet-500/10 px-1 text-violet-600 dark:text-violet-300">{methodHits}×</span>
                                                                </span>
                                                            </button>

                                                            {methodOpen && (
                                                                <div className="ml-3 mt-0.5 space-y-0.5 border-l border-border/40 pl-2">
                                                                    {method.locations.map((loc, idx) => {
                                                                        const active = isActiveLocation(loc);
                                                                        const label = loc.displayName ?? `#${loc.invocation}`;
                                                                        return (
                                                                            <button
                                                                                key={`${loc.invocation}-${idx}`}
                                                                                type="button"
                                                                                onClick={() => onNavigate(loc)}
                                                                                aria-current={active ? 'true' : undefined}
                                                                                className={cn(
                                                                                    'flex w-full items-center justify-between gap-2 rounded-md border-l-2 px-2 py-1 text-left',
                                                                                    active ? 'border-violet-500 bg-violet-500/10' : 'border-transparent hover:bg-accent',
                                                                                )}
                                                                            >
                                                                                <span className={cn('truncate font-mono text-[11px]', active ? 'text-violet-700 dark:text-violet-300' : 'text-muted-foreground')}>{label}</span>
                                                                                <span className="shrink-0 rounded bg-violet-500/10 px-1 font-mono text-[10px] text-violet-600 dark:text-violet-300">{loc.count}×</span>
                                                                            </button>
                                                                        );
                                                                    })}
                                                                </div>
                                                            )}
                                                        </div>
                                                    );
                                                })}
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                ))}
            </div>
        </aside>
    );
};
