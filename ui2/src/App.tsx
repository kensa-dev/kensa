import {useState, useEffect, useRef} from 'react';
import {Beaker, Package, Loader2, Moon, Sun, PanelLeft} from 'lucide-react';
import {AppSidebar} from './components/app-sidebar';
import {SidebarProvider, SidebarInset} from "@/components/ui/sidebar";
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from "@/components/ui/resizable";
import {cn} from "@/lib/utils";
import {ImperativePanelHandle} from "react-resizable-panels";
import {TestCard} from './components/TestCard';
import {IssueBadge} from './components/IssueBadge';
import {Separator} from "./components/ui/separator"
import {ConfigContext, DEFAULT_CONFIG, KensaConfig} from "@/contexts/ConfigContext";
import {useNavigate, useLocation} from 'react-router-dom';
import {isNative} from '@/lib/utils';
import {Index, Indices, SelectedIndex} from "@/types/Index";
import {TestDetail} from "@/types/Test.ts";

const App = () => {
    const [config, setConfig] = useState<KensaConfig>(DEFAULT_CONFIG);
    const [isMounted, setIsMounted] = useState<boolean>(false);
    const [environment, setEnvironment] = useState<string>(() => localStorage.getItem('kensa-env') || "local");
    const [indices, setIndices] = useState<Indices>([]);
    const [selectedIndex, setSelectedIndex] = useState<SelectedIndex | null>(null);
    const [testDetail, setTestDetail] = useState<TestDetail | null>(null);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [darkMode, setDarkMode] = useState<boolean>(localStorage.getItem('theme') === 'dark');
    const [firstFailIndex, setFirstFailIndex] = useState<number>(-1);
    const [isNativeMode, setIsNativeMode] = useState<boolean>(false);

    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        setIsNativeMode(isNative());
    }, []);

    useEffect(() => {
        setIsMounted(true);
    }, []);

    useEffect(() => {
        localStorage.setItem('kensa-env', environment);
    }, [environment]);

    const findNodeById = (nodes: Indices, id: string): Index | null => {
        return nodes.find(node => node.id === id) || null;
    };

    useEffect(() => {
        const match = location.pathname.match(/^\/test\/(.+)$/);
        if (match && indices.length > 0) {
            const id = match[1];
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
        fetch('./configuration.json')
            .then(res => res.json())
            .then(data => setConfig(data))
            .catch(err => console.error("Config fetch failed, using Section constant defaults", err));
    }, []);

    useEffect(() => {
        fetch('./indices.json')
            .then(res => res.json())
            .then(data => setIndices(data.indices || []))
            .catch(err => console.error("Failed to load indices:", err));
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

    return (
        <ConfigContext.Provider value={config}>
            <SidebarProvider>
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
                                onSearchChange={setSearchQuery}
                                environment={environment}
                                onEnvChange={setEnvironment}
                                onSelect={(node) => {
                                    if (node.id.startsWith('pkg:')) return;
                                    navigate(`/test/${node.id}`);
                                }}
                                selectedId={selectedIndex?.id ?? null}
                                isNative={isNativeMode}
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
                                        <Separator orientation="vertical" className="h-4" />

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
                                                            <IssueBadge key={issue} issue={issue} />
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