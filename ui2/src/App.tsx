import {useState, useEffect, useRef, useMemo} from 'react';
import {Beaker, Package, Loader2, Moon, Sun, PanelLeft} from 'lucide-react';
import {AppSidebar} from './components/AppSidebar.tsx';
import {SidebarProvider, SidebarInset} from "@/components/ui/sidebar";
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from "@/components/ui/resizable";
import {cn} from "@/lib/utils";
import {ImperativePanelHandle} from "react-resizable-panels";
import {TestCard} from './components/TestCard';
import {IssueBadge} from './components/IssueBadge';
import {Separator} from "./components/ui/separator"
import {ConfigContext, DEFAULT_CONFIG, KensaConfig} from "@/contexts/ConfigContext";
import {useNavigate, useLocation, useSearchParams} from 'react-router-dom';
import {isNative} from '@/lib/utils';
import {Index, Indices, SelectedIndex} from "@/types/Index";
import {TestDetail} from "@/types/Test.ts";
import {
    CommandDialog,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import {Badge} from "@/components/ui/badge.tsx";

const App = () => {
    const [config, setConfig] = useState<KensaConfig>(DEFAULT_CONFIG);
    const [isMounted, setIsMounted] = useState<boolean>(false);
    const [environment, setEnvironment] = useState<string>(() => localStorage.getItem('kensa-env') || "local");
    const [indices, setIndices] = useState<Indices>([]);
    const [selectedIndex, setSelectedIndex] = useState<SelectedIndex | null>(null);
    const [testDetail, setTestDetail] = useState<TestDetail | null>(null);
    const [searchParams, setSearchParams] = useSearchParams();
    const searchQuery = searchParams.get("q") || "";
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [darkMode, setDarkMode] = useState<boolean>(localStorage.getItem('theme') === 'dark');
    const [firstFailIndex, setFirstFailIndex] = useState<number>(-1);
    const [isNativeMode, setIsNativeMode] = useState<boolean>(false);
    const [open, setOpen] = useState(false);
    const [commandQuery, setCommandQuery] = useState("");
    const searchInputRef = useRef<HTMLInputElement>(null);

    const navigate = useNavigate();
    const location = useLocation();

    const onSearchChange = (query: string) => {
        setSearchParams(prev => {
            if (query) prev.set("q", query);
            else prev.delete("q");
            return prev;
        }, {replace: true});
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
            if (id === 'root-project') {
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
                    state: node.state
                });
                return;
            }
        }
        setSelectedIndex(null);
        setTestDetail(null);
    }, [location.pathname, indices]);

    const sidebarRef = useRef<ImperativePanelHandle>(null);
    const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            const isMod = e.metaKey || e.ctrlKey;

            // Cmd+K: Global Jump
            if (isMod && e.key === "k") {
                e.preventDefault();
                setOpen((prev) => !prev);
                return;
            }

            if (isMod && e.key === "f") {
                e.preventDefault();
                e.stopPropagation();

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

        window.addEventListener("keydown", handleKeyDown, true); // Use capture phase (true)
        return () => window.removeEventListener("keydown", handleKeyDown, true);
    }, [isSidebarCollapsed]);

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
        const loadData = async () => {
            try {
                const configRes = await fetch('./configuration.json');
                if (!configRes.ok) throw new Error(`Config load failed: ${configRes.status}`);
                const configData = await configRes.json();
                setConfig(configData);

                const indicesRes = await fetch('./indices.json');
                if (!indicesRes.ok) throw new Error(`Indices load failed: ${indicesRes.status}`);
                const indicesData = await indicesRes.json();

                const loadedIndices: Indices = indicesData.indices || [];

                if (loadedIndices.length > 0) {
                    const rootNode: Index = {
                        id: 'root-project',
                        type: 'project',
                        displayName: configData.titleText || "Kensa Tests",
                        testClass: '',
                        state: loadedIndices.some(i => i.state === 'Failed') ? 'Failed' : 'Passed',
                        children: loadedIndices
                    };
                    setIndices([rootNode]);
                }
            } catch (err) {
                console.error("Critical error loading Kensa data:", err);
            }
        };

        loadData();
    }, []);

    useEffect(() => {
        if (!selectedIndex) return;
        setIsLoading(true);
        setTestDetail(null);
        setFirstFailIndex(-1);

        fetch(`./results/${selectedIndex.id}.json`)
            .then(res => res.json())
            .then(data => {
                const failIndex = data.tests.findIndex((t: TestDetail) => t.state === 'Failed');
                setFirstFailIndex(failIndex);
                setTestDetail(data);
                setIsLoading(false);
            })
            .catch(() => setIsLoading(false));
    }, [selectedIndex]);

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

    const filteredCommandTests = useMemo(() => {
        if (!commandQuery) return allTests;
        const search = commandQuery.toLowerCase();
        return allTests.filter(test =>
            test.displayName.toLowerCase().includes(search) ||
            test.testClass.toLowerCase().includes(search) ||
            test.projectName?.toLowerCase().includes(search)
        );
    }, [allTests, commandQuery]);

    return (
        <ConfigContext.Provider value={config}>
            <SidebarProvider>
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
                    <ResizablePanelGroup direction="horizontal" className="w-full h-full" autoSaveId="kensa-main-layout">

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
                                searchQuery={searchQuery}
                                onSearchChange={onSearchChange}
                                environment={environment}
                                onEnvChange={setEnvironment}
                                onSelect={(node) => {
                                    if (node.id.startsWith('pkg:') || node.id === 'root-project') {
                                        return;
                                    }
                                    navigate(`/test/${node.id}`);
                                }}
                                selectedId={selectedIndex?.id ?? null}
                                isNative={isNativeMode}
                                inputRef={searchInputRef}
                            />
                        </ResizablePanel>

                        <ResizableHandle withHandle className={cn(
                            isSidebarCollapsed && "hidden",
                            !isSidebarCollapsed && "bg-border/50 hover:bg-primary/30 transition-colors w-1"
                        )}/>

                        <ResizablePanel defaultSize={80} className="flex flex-col h-full overflow-hidden">
                            <SidebarInset className="flex flex-col h-full overflow-hidden">
                                <header className="sticky top-0 z-10 flex h-12 shrink-0 items-center justify-between bg-background/80 backdrop-blur px-4 border-b">
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={toggleSidebar}
                                            className="p-2 hover:bg-accent/50 rounded-md text-muted-foreground transition-colors mr-1"
                                        >
                                            <PanelLeft size={18} className={cn(isSidebarCollapsed && "opacity-50")}/>
                                        </button>
                                        <Separator orientation="vertical" className="h-4"/>

                                        {selectedIndex && (
                                            <div className="flex flex-col py-1 ml-2">
                                                <div className="flex items-center gap-1.5 text-[11px] font-mono tracking-wider opacity-70 text-neutral-800 dark:text-neutral-100">
                                                    <Package size={10}/>
                                                    {selectedIndex.testClass}
                                                </div>
                                                <div className="flex items-center gap-3">
                                                    <h1 className="text-[14px] font-black truncate text-neutral-800 dark:text-neutral-100 leading-tight">
                                                        {selectedIndex.displayName}
                                                    </h1>
                                                    <div className="flex items-center gap-1">
                                                        {testDetail?.issues?.map((issue: string) => (
                                                            <IssueBadge key={issue} issue={issue}/>
                                                        ))}
                                                    </div>
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

                                <main className="flex-1 overflow-y-auto bg-muted/30">
                                    {selectedIndex ? (
                                        <div className="p-6 lg:p-8">
                                            <div className="max-w-[1400px] mx-auto">
                                                {isLoading ? (
                                                    <div className="h-64 flex flex-col items-center justify-center text-muted-foreground">
                                                        <Loader2 size={32} className="animate-spin mb-2 text-primary"/>
                                                        <p>Loading result details...</p>
                                                    </div>
                                                ) : testDetail ? (
                                                    testDetail.tests.map((test, i: number) => (
                                                        <TestCard
                                                            key={`${testDetail.testClass}-${i}`}
                                                            test={test}
                                                            initialExpanded={i === firstFailIndex}
                                                        />
                                                    ))
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
                </div>
            </SidebarProvider>
        </ConfigContext.Provider>
    );
};

export default App;