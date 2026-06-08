import {Fragment, useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {Beaker, Loader2, Moon, PanelLeft, Sun} from 'lucide-react';
import {AppSidebar} from './components/AppSidebar';
import {SidebarInset, SidebarProvider} from "@/components/ui/sidebar";
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from "@/components/ui/resizable";
import {applySuiteHighlights, cn, isNative, loadJson, removeHighlightSpans, SUITE_HIGHLIGHT_CLASS, useNavigateWithSearch} from "@/lib/utils";
import {loadManifestOrFallback} from "@/lib/manifestLoader";
import {ImperativePanelHandle} from "react-resizable-panels";
import {ConfigContext, DEFAULT_CONFIG, KensaConfig} from "@/contexts/ConfigContext";
import {SourceContext} from "@/contexts/SourceContext";
import {useLocation, useSearchParams} from 'react-router-dom';
import {Index, Indices, SelectedIndex} from "@/types/Index";
import {TestDetail} from "@/types/Test";
import {IssueList} from './components/IssueList';
import {CommandDialog, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList,} from "@/components/ui/command"
import {Badge} from "@/components/ui/badge";
import {Breadcrumb, BreadcrumbItem, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator} from "@/components/ui/breadcrumb";
import {TestContainer} from './components/TestContainer';
import {NotesCard} from './components/NotesCard';
import {SystemViewPage} from './components/SystemViewPage';
import {TooltipProvider} from "@/components/ui/tooltip";
import {hasOpenDialog, shouldClearSearchOnEscape} from "@/util/escapeGuard";
import {nextQueryAfterTagClick, selectedTagsFromQuery} from "@/util/tagClick";
import {TagFilterProvider} from "@/contexts/TagFilterContext";
import {SuiteSearchProvider, type SuiteSearchResult} from "@/contexts/SuiteSearchContext";
import {SuiteSearchResults} from "@/components/SuiteSearchResults";
import {mergeIndexes, type MergedIndex} from "@/lib/suiteSearch";
import type {RawSearchIndex} from "@/types/SearchIndex";
import {nodeIdForLocation} from "@/util/suiteSearchNav";

const tagWithSourceId = (nodes: Indices, sourceId: string): Indices =>
    nodes.map(n => ({
        ...n,
        id: n.testClass ? `${sourceId}::${n.id}` : n.id,
        sourceId,
        children: n.children ? tagWithSourceId(n.children, sourceId) : undefined,
    }));

const App = () => {
    const [config, setConfig] = useState<KensaConfig>(DEFAULT_CONFIG);
    const [isMounted, setIsMounted] = useState<boolean>(false);
    const [environment, setEnvironment] = useState<string>(() => localStorage.getItem('kensa-env') || "local");
    const [indices, setIndices] = useState<Indices>([]);
    const [aggregateComponentDiagramsBySource, setAggregateComponentDiagramsBySource] = useState<Record<string, string>>({});
    const [selectedIndex, setSelectedIndex] = useState<SelectedIndex | null>(null);
    const [testDetail, setTestDetail] = useState<TestDetail | null>(null);
    const [searchParams, setSearchParams] = useSearchParams();
    const searchQuery = searchParams.get("q") || "";
    const [testToExpand, setTestToExpand] = useState<string>("");
    const [matchingMethods, setMatchingMethods] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [darkMode, setDarkMode] = useState<boolean>(localStorage.getItem('theme') === 'dark');
    const [isNativeMode, setIsNativeMode] = useState<boolean>(false);
    const [open, setOpen] = useState(false);
    const [commandQuery, setCommandQuery] = useState("");
    const [sourceConfigs, setSourceConfigs] = useState<Record<string, KensaConfig>>({});
    const [sourceUrls, setSourceUrls] = useState<Record<string, string>>({});
    const [sourceTitles, setSourceTitles] = useState<Record<string, string>>({});
    const [mergedSearchIndex, setMergedSearchIndex] = useState<MergedIndex>({terms: []});
    const [suiteHighlightValue, setSuiteHighlightValue] = useState<string | null>(null);
    const [selectedResultKey, setSelectedResultKey] = useState<string | null>(null);
    const pendingSuiteHighlightRef = useRef<{testId: string; value: string} | null>(null);
    const searchInputRef = useRef<HTMLInputElement>(null);
    const [testToExpandInvocation, setTestToExpandInvocation] = useState<number>(-1);
    const pendingTestToExpandRef = useRef<{testId: string, method: string, invocation?: number} | null>(null);
    const searchQueryRef = useRef(searchQuery);
    const commandOpenRef = useRef(open);
    const suiteHighlightRef = useRef<string | null>(null);
    searchQueryRef.current = searchQuery;
    commandOpenRef.current = open;
    suiteHighlightRef.current = suiteHighlightValue;

    const navigate = useNavigateWithSearch();
    const location = useLocation();

    const onSearchChange = (query: string) => {
        setSearchParams(prev => {
            if (query) prev.set("q", query);
            else prev.delete("q");
            return prev;
        }, {replace: true});

        if (!query) {
            setMatchingMethods([]);
        }
    };

    const onTagClick = useCallback(
        (tag: string, additive: boolean) => onSearchChange(nextQueryAfterTagClick(searchQueryRef.current, tag, additive)),
        [],
    );

    const selectedTags = useMemo(() => selectedTagsFromQuery(searchQuery), [searchQuery]);

    const handleFilterApplied = (firstTest: Index | null, firstMethod: string | null, matchingMethodsMap: Map<string, string[]>) => {
        if (firstTest && firstTest.id) {
            if (selectedIndex?.id !== firstTest.id) {
                navigate(`/test/${firstTest.id}`, {replace: true});
            }
            setTestToExpand(firstMethod || "");
        } else {
            setTestToExpand("");
        }
        setTestToExpandInvocation(-1);

        if (selectedIndex?.id) {
            const methods = matchingMethodsMap.get(selectedIndex.id) || [];
            setMatchingMethods(methods);
        }
    };

    useEffect(() => {
        setIsNativeMode(isNative());
    }, []);

    useEffect(() => {
        setIsMounted(true);
    }, []);

    useEffect(() => {
        localStorage.setItem('kensa-env', environment);
    }, [environment]);

    useEffect(() => {
        const match = location.pathname.match(/^\/test\/(.+)$/);
        if (match && indices.length > 0) {
            const id = match[1];
            if (id.startsWith('src:')) {
                setSelectedIndex(null);
                setTestDetail(null);
                return;
            }
            const node = findNodeById(indices, id);
            if (node) {
                setSelectedIndex({
                    id: node.id,
                    displayName: node.displayName,
                    testClass: node.testClass,
                    state: node.state,
                    sourceId: node.sourceId,
                });
                const method = searchParams.get('method');
                if (method) {
                    const invocationParam = searchParams.get('invocation');
                    pendingTestToExpandRef.current = { testId: node.id, method, invocation: invocationParam !== null ? Number(invocationParam) : undefined };
                }
                return;
            }
        }
        setSelectedIndex(null);
        setTestDetail(null);
    }, [location.pathname, location.search, indices]);

    const sidebarRef = useRef<ImperativePanelHandle>(null);
    const contentRef = useRef<HTMLDivElement>(null);
    const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const isMod = e.metaKey || e.ctrlKey;
            const tag = (e.target as HTMLElement)?.tagName;
            const isEditable = tag === "INPUT" || tag === "TEXTAREA" || (e.target as HTMLElement)?.isContentEditable;

            if (isMod && e.key === "k") {
                e.preventDefault();
                setOpen((prev) => !prev);
                return;
            }

            if (!isEditable && !isMod && e.key === "/") {
                e.preventDefault();

                if (isSidebarCollapsed) {
                    sidebarRef.current?.expand();
                }

                setTimeout(() => {
                    searchInputRef.current?.focus();
                    searchInputRef.current?.select();
                }, 50);
                return;
            }
        };

        window.addEventListener("keydown", handleKeyDown, true);
        return () => window.removeEventListener("keydown", handleKeyDown, true);
    }, [isSidebarCollapsed]);

    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key !== "Escape") return;

            const dialogOpen = hasOpenDialog();

            if (!dialogOpen && suiteHighlightRef.current) {
                e.preventDefault();
                setSuiteHighlightValue(null);
                return;
            }

            const clear = shouldClearSearchOnEscape({
                hasQuery: !!searchQueryRef.current,
                isCommandOpen: commandOpenRef.current,
                isDialogOpen: dialogOpen,
            });
            if (clear) {
                e.preventDefault();
                onSearchChange('');
            }
        };

        document.addEventListener("keydown", handleEscape, true);
        return () => document.removeEventListener("keydown", handleEscape, true);
    }, []);

    const findNodeById = (nodes: Indices, id: string): Index | null => {
        for (const node of nodes) {
            if (node.id === id) return node;
            if (node.children && node.children.length > 0) {
                const found = findNodeById(node.children, id);
                if (found) return found;
            }
        }
        return null;
    };

    useEffect(() => {
        const loadInitialData = async () => {
            const manifest = await loadManifestOrFallback();

            const perSource = await Promise.all(manifest.sources.map(async (source) => {
                const config = await loadJson<KensaConfig>(
                    'configuration.json',
                    `Configuration (${source.id})`,
                    {baseUrl: source.url}
                );
                const indicesData = await loadJson<{ indices: Indices; aggregateComponentDiagram?: string }>(
                    'indices.json',
                    `Indices tree (${source.id})`,
                    {baseUrl: source.url}
                );
                const searchIndex = await loadJson<RawSearchIndex>(
                    'search-index.json',
                    `Search index (${source.id})`,
                    {baseUrl: source.url}
                );
                return {source, config, indicesData, searchIndex};
            }));

            const sourceConfigsMap: Record<string, KensaConfig> = {};
            const roots: Indices = [];
            const diagramsBySource: Record<string, string> = {};
            const perSourceSearch: { sourceId: string; index: RawSearchIndex }[] = [];
            for (const {source, config, indicesData, searchIndex} of perSource) {
                perSourceSearch.push({
                    sourceId: source.id,
                    index: searchIndex ?? {schemaVersion: 1, terms: []},
                });
                if (config) sourceConfigsMap[source.id] = config;
                const sourceIndices = indicesData?.indices ?? [];
                const sourceDiagram = indicesData?.aggregateComponentDiagram;
                if (sourceDiagram) diagramsBySource[source.id] = sourceDiagram;
                if (sourceIndices.length === 0) continue;
                const tagged = tagWithSourceId(sourceIndices, source.id);
                // Each source root's first child is a synthetic "System View" entry pointing at
                // that source's own aggregate component diagram — keeps per-source architectures
                // separate in the sidebar tree rather than conflating them under one global view.
                const children: Indices = sourceDiagram
                    ? [
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
                    : tagged;
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

            setSourceConfigs(sourceConfigsMap);
            setSourceUrls(urls);
            setSourceTitles(titles);
            setMergedSearchIndex(mergeIndexes(perSourceSearch));
            setIndices(roots);
            setAggregateComponentDiagramsBySource(diagramsBySource);
            // First source's config flows through ConfigContext until the user selects a test;
            // selection updates it via the selectedIndex effect below.
            const first = manifest.sources[0];
            if (first && sourceConfigsMap[first.id]) setConfig(sourceConfigsMap[first.id]);
        };

        void loadInitialData();
    }, []);

    useEffect(() => {
        if (!selectedIndex) return;

        const loadTestDetail = async () => {
            setIsLoading(true);
            setTestDetail(null);

            const sourceId = selectedIndex.sourceId;
            const baseUrl = sourceId ? (sourceUrls[sourceId] ?? '.') : '.';
            const localId = sourceId ? selectedIndex.id.replace(`${sourceId}::`, '') : selectedIndex.id;

            const data = await loadJson<TestDetail>(
                `results/${localId}.json`,
                `Test results (${selectedIndex.displayName})`,
                {baseUrl}
            );

            if (data) {
                setTestDetail(data);

                if (pendingTestToExpandRef.current?.testId === selectedIndex.id) {
                    setTestToExpand(pendingTestToExpandRef.current.method);
                    setTestToExpandInvocation(pendingTestToExpandRef.current.invocation ?? -1);
                    pendingTestToExpandRef.current = null;
                }

                if (pendingSuiteHighlightRef.current?.testId === selectedIndex.id) {
                    setSuiteHighlightValue(pendingSuiteHighlightRef.current.value);
                    pendingSuiteHighlightRef.current = null;
                }
            }

            setIsLoading(false);
        };

        void loadTestDetail();
    }, [selectedIndex, sourceUrls]);

    useEffect(() => {
        if (!selectedIndex?.sourceId) return;
        const cfg = sourceConfigs[selectedIndex.sourceId];
        if (cfg) setConfig(cfg);
    }, [selectedIndex, sourceConfigs]);

    useEffect(() => {
        const root = contentRef.current;
        if (!root) return;

        removeHighlightSpans(root, SUITE_HIGHLIGHT_CLASS);
        if (!suiteHighlightValue) return;

        const apply = () => {
            removeHighlightSpans(root, SUITE_HIGHLIGHT_CLASS);
            applySuiteHighlights(root, [suiteHighlightValue]);
        };

        // Apply once now, then re-apply once if content (expanded methods, interactions)
        // rendered after this commit. A live MutationObserver here ping-pongs with React
        // re-renders (e.g. hover state reverting injected spans) and flickers, so we instead
        // re-apply at most once, only when the highlight is still missing.
        const frame = window.requestAnimationFrame(apply);
        const settle = window.setTimeout(() => {
            if (!root.querySelector('.' + SUITE_HIGHLIGHT_CLASS)) apply();
        }, 400);

        return () => {
            window.cancelAnimationFrame(frame);
            window.clearTimeout(settle);
            removeHighlightSpans(root, SUITE_HIGHLIGHT_CLASS);
        };
    }, [suiteHighlightValue, testDetail, testToExpand, isLoading]);

    useEffect(() => {
        const root = window.document.documentElement;
        darkMode ? root.classList.add("dark") : root.classList.remove("dark");
        localStorage.setItem('theme', darkMode ? 'dark' : 'light');
    }, [darkMode]);

    const toggleSidebar = () => {
        const sidebar = sidebarRef.current;
        if (sidebar) {
            if (isSidebarCollapsed) {
                sidebar.expand();
            } else {
                sidebar.collapse();
            }
        }
    };

    const allTests = useMemo(() => {
        const tests: (Index & { projectName?: string, envName?: string })[] = [];
        const collect = (nodes: Indices, currentProject?: string) => {
            nodes.forEach(n => {
                const projectContext = n.type === 'project' ? n.displayName : currentProject;
                if (n.testClass) {
                    tests.push({...n, projectName: projectContext, envName: environment});
                }
                if (n.children) collect(n.children, projectContext);
            });
        };
        collect(indices);
        return tests;
    }, [indices, environment]);

    const handleTestLink = useCallback((ref: string) => {
        const dotIndex = ref.lastIndexOf('.');
        if (dotIndex === -1) {
            // No dot — could be a method in the current suite, or a class name
            const matchedSuite = allTests.find(t => t.testClass.split('.').pop() === ref);
            if (matchedSuite) {
                navigate(`/test/${matchedSuite.id}`);
            } else {
                setTestToExpand(ref);
            }
        } else {
            // ClassName.methodName — navigate to suite and expand method
            const className = ref.slice(0, dotIndex);
            const methodName = ref.slice(dotIndex + 1);
            const matchedSuite = allTests.find(t => t.testClass.split('.').pop() === className);
            if (matchedSuite) {
                if (selectedIndex?.id === matchedSuite.id) {
                    setTestToExpand(methodName);
                } else {
                    pendingTestToExpandRef.current = { testId: matchedSuite.id, method: methodName };
                    navigate(`/test/${matchedSuite.id}`);
                }
            }
        }
    }, [allTests, selectedIndex, navigate]);

    const handleSuiteSearchNavigate = useCallback((location: SuiteSearchResult) => {
        const nodeId = nodeIdForLocation(location);
        pendingSuiteHighlightRef.current = {testId: nodeId, value: location.value};
        setSelectedResultKey(`${nodeId}|${location.testMethod}|${location.invocation}`);

        if (selectedIndex?.id === nodeId) {
            setSuiteHighlightValue(location.value);
            setTestToExpand(location.testMethod);
            setTestToExpandInvocation(location.invocation);
            pendingSuiteHighlightRef.current = null;
        } else {
            setSuiteHighlightValue(null);
            pendingTestToExpandRef.current = {testId: nodeId, method: location.testMethod, invocation: location.invocation};
            navigate(`/test/${nodeId}?method=${encodeURIComponent(location.testMethod)}&invocation=${location.invocation}`);
        }
    }, [selectedIndex, navigate]);

    const filteredCommandTests = useMemo(() => {
        if (!commandQuery) return allTests;
        const search = commandQuery.toLowerCase();
        return allTests.filter(test =>
            test.displayName.toLowerCase().includes(search) ||
            test.testClass.toLowerCase().includes(search) ||
            test.projectName?.toLowerCase().includes(search)
        );
    }, [allTests, commandQuery]);

    const isSystemView = location.pathname === '/system-view';
    const systemViewSourceId = searchParams.get('source') ?? Object.keys(aggregateComponentDiagramsBySource)[0];
    const systemViewDiagram = systemViewSourceId ? aggregateComponentDiagramsBySource[systemViewSourceId] ?? "" : "";

    const activeSourceBaseUrl = selectedIndex?.sourceId ? (sourceUrls[selectedIndex.sourceId] ?? '.') : '.';

    const sourceMetaById = useMemo(() => {
        const out: Record<string, { generatedAt?: string }> = {};
        for (const [id, cfg] of Object.entries(sourceConfigs)) {
            if (cfg.generatedAt) out[id] = {generatedAt: cfg.generatedAt};
        }
        return out;
    }, [sourceConfigs]);

    return (
        <ConfigContext.Provider value={config}>
            <SourceContext.Provider value={{baseUrl: activeSourceBaseUrl}}>
            <SuiteSearchProvider mergedIndex={mergedSearchIndex} highlightValue={suiteHighlightValue} onHighlightValue={setSuiteHighlightValue}>
            <TooltipProvider>
            <SidebarProvider>
            <TagFilterProvider onTagClick={onTagClick} selectedTags={selectedTags}>
                <CommandDialog open={open} onOpenChange={setOpen}>
                    <CommandInput
                        placeholder="Jump to test..."
                        value={commandQuery}
                        onValueChange={setCommandQuery}
                    />
                    <CommandList className="h-[400px] overflow-y-auto">
                        {filteredCommandTests.length === 0 && <CommandEmpty>No tests found.</CommandEmpty>}
                        <CommandGroup heading="Test Suites">
                            {filteredCommandTests.map((test) => (
                                <CommandItem
                                    key={`${test.projectName}-${test.id}`}
                                    value={test.id}
                                    onSelect={() => {
                                        navigate(`/test/${test.id}`);
                                        setOpen(false);
                                        setCommandQuery("");
                                    }}
                                    className="flex items-center gap-3 py-3"
                                >
                                    <div className={cn(
                                        "w-1.5 h-1.5 rounded-full shrink-0",
                                        test.state === 'Failed' ? "bg-destructive" : "bg-emerald-500/50"
                                    )}/>

                                    <div className="flex flex-col flex-1 min-w-0">
                                        <div className="flex items-center gap-2">
                                            <span className="font-semibold text-sm truncate">{test.displayName}</span>
                                            {test.state === 'Failed' && (
                                                <Badge variant="outline" className="text-[8px] h-3.5 px-1 border-destructive/30 text-destructive/70 bg-destructive/5 uppercase">
                                                    Failed
                                                </Badge>
                                            )}
                                        </div>
                                        <div className="flex items-center gap-1.5 text-[10px] opacity-50 font-mono truncate">
                                            <span className="text-blue-500 font-bold uppercase">{test.envName}</span>
                                            <span>/</span>
                                            <span className="font-bold text-foreground/70">{test.projectName}</span>
                                            <span>/</span>
                                            <span>{test.testClass}</span>
                                        </div>
                                    </div>
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </CommandDialog>

                <div className="flex h-screen w-full bg-background font-sans overflow-hidden">
                    <ResizablePanelGroup direction="horizontal" className="h-full flex-1 min-w-0" autoSaveId="kensa-main-layout">

                        <ResizablePanel
                            ref={sidebarRef}
                            defaultSize={20}
                            minSize={15}
                            maxSize={40}
                            collapsible={true}
                            collapsedSize={0}
                            onCollapse={() => setIsSidebarCollapsed(true)}
                            onExpand={() => setIsSidebarCollapsed(false)}
                            className={cn(
                                "h-full overflow-visible",
                                isMounted && "transition-all duration-300"
                            )}
                        >
                            <AppSidebar
                                indices={indices}
                                sourceMetaById={sourceMetaById}
                                searchQuery={searchQuery}
                                onSearchChange={onSearchChange}
                                environment={environment}
                                onEnvChange={setEnvironment}
                                onSelect={(node, firstMatchingMethod, allMatchingMethods) => {
                                    if (node.id.startsWith('pkg:') || node.id.startsWith('src:')) {
                                        return;
                                    }
                                    if (node.type === 'system-view' && node.sourceId) {
                                        navigate(`/system-view?source=${encodeURIComponent(node.sourceId)}`);
                                        return;
                                    }

                                    if (selectedIndex?.id === node.id) {
                                        setTestToExpand(firstMatchingMethod || "");
                                        setTestToExpandInvocation(-1);
                                        setMatchingMethods(allMatchingMethods);
                                    } else {
                                        pendingTestToExpandRef.current = {
                                            testId: node.id,
                                            method: firstMatchingMethod || ""
                                        };
                                        setTestToExpand("");
                                        setMatchingMethods(allMatchingMethods);
                                        navigate(`/test/${node.id}`, {replace: true});
                                    }
                                }}
                                selectedId={selectedIndex?.id ?? null}
                                selectedResultKey={selectedResultKey}
                                isNative={isNativeMode}
                                inputRef={searchInputRef}
                                onFilterApplied={handleFilterApplied}
                            />
                        </ResizablePanel>

                        <ResizableHandle withHandle className={cn(
                            isSidebarCollapsed && "hidden",
                            !isSidebarCollapsed && "bg-border/50 hover:bg-primary/30 transition-colors w-1"
                        )}/>

                        <ResizablePanel defaultSize={80} className="flex flex-col h-full overflow-hidden">
                            <SidebarInset className="flex flex-col h-full overflow-hidden">
                                <header
                                    className={cn(
                                        "sticky top-0 z-10 flex h-16 shrink-0 items-center justify-between backdrop-blur px-4 border-b",
                                        selectedIndex?.state === "Passed"
                                            ? "bg-emerald-500/10 border-emerald-500/30"
                                            : selectedIndex?.state === "Failed"
                                                ? "bg-rose-500/10 border-rose-500/30"
                                                : selectedIndex?.state === "Disabled"
                                                    ? "bg-disabled/10 border-disabled/30"
                                                    : "bg-background/80 border-border"
                                    )}
                                >
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={toggleSidebar}
                                            className="p-2 hover:bg-accent/50 rounded-md text-muted-foreground transition-colors mr-1"
                                        >
                                            <PanelLeft size={18} className={cn(isSidebarCollapsed && "opacity-50")}/>
                                        </button>

                                        {selectedIndex && (
                                            <div
                                                className={cn(
                                                    "flex flex-col py-1 ml-2",
                                                    "rounded-lg px-2 -mx-2 transition-colors",
                                                    "py-1.5 gap-0.5"
                                                )}
                                            >
                                                <Breadcrumb>
                                                    <BreadcrumbList className="font-mono text-[10px] tracking-wide opacity-60 flex-nowrap">
                                                        {selectedIndex.testClass.split('.').map((segment, i, arr) => {
                                                            const fullPackage = arr.slice(0, i + 1).join('.');
                                                            const pkgToken = `pkg:${fullPackage}`;
                                                            const tokens = searchQuery.split(/\s+/);
                                                            const isActive = tokens.includes(pkgToken);
                                                            const toggle = () => {
                                                                const without = tokens.filter(t => !t.startsWith('pkg:'));
                                                                onSearchChange(isActive ? without.join(' ').trim() : [...without, pkgToken].join(' ').trim());
                                                            };
                                                            return i < arr.length - 1 ? (
                                                                <Fragment key={i}>
                                                                    <BreadcrumbItem>
                                                                        <button
                                                                            onClick={toggle}
                                                                            className={cn(
                                                                                "transition-colors underline-offset-2",
                                                                                isActive
                                                                                    ? "text-indigo-500 dark:text-indigo-400 underline"
                                                                                    : "text-neutral-600 dark:text-neutral-400 hover:text-indigo-500 dark:hover:text-indigo-400 hover:underline"
                                                                            )}
                                                                        >
                                                                            {segment}
                                                                        </button>
                                                                    </BreadcrumbItem>
                                                                    <BreadcrumbSeparator />
                                                                </Fragment>
                                                            ) : (
                                                                <BreadcrumbItem key={i}>
                                                                    <BreadcrumbPage className="font-mono text-[10px] font-semibold text-neutral-700 dark:text-neutral-200">{segment}</BreadcrumbPage>
                                                                </BreadcrumbItem>
                                                            );
                                                        })}
                                                    </BreadcrumbList>
                                                </Breadcrumb>
                                                <div className="flex items-center gap-3">
                                                    <h1 className="text-[14px] font-black truncate text-neutral-800 dark:text-neutral-100 leading-tight">
                                                        {selectedIndex.displayName}
                                                    </h1>
                                                    <IssueList issues={testDetail?.issues} testState={selectedIndex.state} />
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <button
                                        onClick={() => setDarkMode(!darkMode)}
                                        className="p-2 hover:bg-accent/50 rounded-md text-muted-foreground transition-colors"
                                    >
                                        {darkMode ? <Sun size={16}/> : <Moon size={16}/>}
                                    </button>
                                </header>

                                <main ref={contentRef} className="flex-1 overflow-y-auto bg-muted/30">
                                    {isSystemView ? (
                                        <SystemViewPage aggregateComponentDiagram={systemViewDiagram}/>
                                    ) : selectedIndex ? (
                                        <div className="p-6 lg:p-4">
                                            <div className="max-w-[1400px] mx-auto">
                                                {isLoading ? (
                                                    <div className="h-64 flex flex-col items-center justify-center text-muted-foreground">
                                                        <Loader2 size={32} className="animate-spin mb-2 text-primary"/>
                                                        <p>Loading result details...</p>
                                                    </div>
                                                ) : testDetail ? (
                                                    <div className="space-y-4">
                                                        {testDetail.notes && (
                                                            <NotesCard notes={testDetail.notes} onTestLink={handleTestLink} />
                                                        )}
                                                        <TestContainer
                                                            key={`${selectedIndex?.id}-${testToExpand}-${searchQuery}`}
                                                            tests={testDetail.tests}
                                                            testClass={testDetail.testClass}
                                                            testToExpand={testToExpand}
                                                            invocationToExpand={testToExpandInvocation}
                                                            matchingMethods={matchingMethods}
                                                            onClearFilter={() => {
                                                                setMatchingMethods([]);
                                                            }}
                                                        />
                                                    </div>
                                                ) : null}
                                            </div>
                                        </div>
                                    ) : (
                                        <div className="h-full flex flex-col items-center justify-center text-muted-foreground">
                                            <Beaker size={64} className="mb-4 opacity-10"/>
                                            <p className="text-xl font-light italic text-center">Select a test suite from the explorer<br/><span className="text-sm opacity-50">or use the environment switcher above</span></p>
                                        </div>
                                    )}
                                </main>
                            </SidebarInset>
                        </ResizablePanel>
                    </ResizablePanelGroup>
                    <SuiteSearchResults sourceTitles={sourceTitles} onNavigate={handleSuiteSearchNavigate} selectedNodeId={selectedIndex?.id ?? null} selectedResultKey={selectedResultKey} />
                </div>
            </TagFilterProvider>
            </SidebarProvider>
            </TooltipProvider>
            </SuiteSearchProvider>
            </SourceContext.Provider>
        </ConfigContext.Provider>
    );
};

export default App;
