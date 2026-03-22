import {useEffect, useRef, useState} from 'react';
import {InteractionCard} from './InteractionCard';
import {InteractionDialog} from './InteractionDialog';
import {cn} from "@/lib/utils";
import {Tab} from "@/constants";
import {Interaction, Invocation, TestState} from "@/types/Test";
import {SequenceDiagram} from "@/components/SequenceDiagram";
import {loadText} from "@/lib/utils";
import {CustomTabPanel} from "@/components/CustomTabPanel";
import {DataTable} from "@/components/DataTable";
import {Tabs as ShadcnTabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";

type TabValue = typeof Tab[keyof typeof Tab];

interface TabProps {
    invocation: Invocation;
    testState: TestState;
    autoOpenTab?: string;
}

export const Tabs = ({invocation, testState, autoOpenTab}: TabProps) => {
    const isPassed = testState === 'Passed';

    const builtinTabs = [
        {id: Tab.Givens, label: 'Givens', count: invocation[Tab.Givens]?.length},
        {id: Tab.CapturedInteractions, label: 'Interactions', count: invocation[Tab.CapturedInteractions]?.length},
        {id: Tab.CapturedOutputs, label: 'Outputs', count: invocation[Tab.CapturedOutputs]?.length},
        {id: Tab.Fixtures, label: 'Fixtures', count: invocation[Tab.Fixtures]?.length},
        {id: Tab.SequenceDiagram, label: 'Sequence Diagram', exists: !!invocation[Tab.SequenceDiagram]},
    ].filter(t => (t.count && t.count > 0) || t.exists);

    const customTabs = (invocation.customTabContents ?? []).map((t) => ({
        id: t.tabId,
        label: t.label,
        file: t.file,
        mediaType: t.mediaType ?? 'text/plain',
        kind: 'custom' as const,
    }));

    const tabs = [
        ...builtinTabs.map(t => ({...t, kind: 'builtin' as const})),
        ...customTabs
    ];

    const [selectedInteraction, setSelectedInteraction] = useState<Interaction | null>(null);
    const svgRef = useRef<HTMLDivElement>(null);

    const availableTabIds = tabs.map(t => t.id);

    const resolveTab = (incoming?: string): (TabValue | string) | undefined => {
        if (!incoming) return undefined;

        if (incoming in Tab) {
            return Tab[incoming as keyof typeof Tab];
        }

        return availableTabIds.find(id => id === incoming);
    };

    const [activeTab, setActiveTab] = useState<(TabValue | string)>(() => {
        return resolveTab(autoOpenTab) || availableTabIds[0] || Tab.Givens;
    });

    useEffect(() => {
        const resolved = resolveTab(autoOpenTab);
        if (resolved) {
            setActiveTab(resolved);
        }
    }, [autoOpenTab, invocation]);

    useEffect(() => {
        if (activeTab === Tab.SequenceDiagram && svgRef.current) {
            const clickHandler = (e: MouseEvent) => {
                const target = e.target as SVGElement;
                const interactionId = target.getAttribute('sequence_diagram_interaction_id');
                if (interactionId) {
                    const found = invocation[Tab.CapturedInteractions].find((i: Interaction) => i.id === interactionId);
                    if (found) setSelectedInteraction(found);
                }
            };
            svgRef.current.addEventListener('click', clickHandler);
            return () => svgRef.current?.removeEventListener('click', clickHandler);
        }
    }, [activeTab, invocation]);

    const [customTabCache, setCustomTabCache] = useState<Record<string, {status: 'idle' | 'loading' | 'ready' | 'error', content?: string}>>({});

    const inFlightRef = useRef<Map<string, AbortController>>(new Map());

    const activeCustomTab = tabs.find(t => t.kind === 'custom' && t.id === activeTab) as
        | { kind: 'custom', id: string, label: string, file: string, mediaType: string }
        | undefined;

    const activeCustomFile = activeCustomTab?.file;
    const activeCustomLabel = activeCustomTab?.label ?? 'Custom tab';

    useEffect(() => {
        if (!activeCustomFile) return;

        const cached = customTabCache[activeCustomFile];
        if (cached?.status === 'ready' || cached?.status === 'loading') return;

        if (inFlightRef.current.has(activeCustomFile)) return;

        const controller = new AbortController();
        inFlightRef.current.set(activeCustomFile, controller);

        setCustomTabCache(prev => ({
            ...prev,
            [activeCustomFile]: {status: 'loading'}
        }));

        loadText(activeCustomFile, `custom tab "${activeCustomLabel}"`, { signal: controller.signal })
            .then((content) => {
                if (controller.signal.aborted) return;

                if (content == null) {
                    setCustomTabCache(prev => ({
                        ...prev,
                        [activeCustomFile]: {status: 'error'}
                    }));
                    return;
                }

                setCustomTabCache(prev => ({
                    ...prev,
                    [activeCustomFile]: {status: 'ready', content}
                }));
            })
            .catch(() => {
                if (controller.signal.aborted) return;
                setCustomTabCache(prev => ({
                    ...prev,
                    [activeCustomFile]: {status: 'error'}
                }));
            })
            .finally(() => {
                inFlightRef.current.delete(activeCustomFile);
            });

        return () => {
            controller.abort();
            inFlightRef.current.delete(activeCustomFile);
        };
    }, [activeCustomFile, activeCustomLabel]);

    if (tabs.length === 0) return null;

    const tabTriggerClass = cn(
        "px-4 py-2 text-[11px] font-bold tracking-wider rounded-none border-b-2 transition-all whitespace-nowrap h-auto",
        "data-[state=active]:bg-card data-[state=active]:shadow-none",
        isPassed
            ? "data-[state=active]:text-success data-[state=active]:border-success/50 data-[state=inactive]:text-muted-foreground data-[state=inactive]:border-transparent data-[state=inactive]:hover:bg-muted/60 data-[state=inactive]:hover:text-foreground"
            : "data-[state=active]:text-failure data-[state=active]:border-failure/50 data-[state=inactive]:text-muted-foreground data-[state=inactive]:border-transparent data-[state=inactive]:hover:bg-muted/60 data-[state=inactive]:hover:text-foreground"
    );

    const contentBg = "p-4 transition-colors";

    return (
        <>
            <ShadcnTabs
                value={activeTab}
                onValueChange={setActiveTab}
                className={cn(
                    "border rounded-lg overflow-hidden bg-card shadow-sm transition-colors",
                    isPassed ? "border-success/40" : "border-failure/40"
                )}
            >
                <TabsList className="w-full flex rounded-none h-auto border-b border-border/60 p-0 overflow-x-auto justify-start bg-muted/30">
                    {tabs.map((tab) => (
                        <TabsTrigger key={tab.id} value={tab.id} className={tabTriggerClass}>
                            {tab.label}
                            {'count' in tab && tab.count != null && tab.count > 0 && (
                                <span className={cn(
                                    "ml-1.5 inline-flex items-center justify-center rounded-full px-1.5 py-0 min-w-[18px] h-[16px] text-[10px] font-bold tabular-nums leading-none",
                                    "bg-foreground/8 text-muted-foreground",
                                    "data-[state=active]:bg-current/10"
                                )}>
                                    {tab.count}
                                </span>
                            )}
                        </TabsTrigger>
                    ))}
                </TabsList>

                <div className={contentBg}>
                    <TabsContent value={Tab.Givens} className="mt-0">
                        <DataTable data={invocation[Tab.Givens]} testState={testState} highlights={invocation.highlights}/>
                    </TabsContent>

                    <TabsContent value={Tab.CapturedOutputs} className="mt-0">
                        <DataTable data={invocation[Tab.CapturedOutputs]} testState={testState} highlights={invocation.highlights}/>
                    </TabsContent>

                    <TabsContent value={Tab.Fixtures} className="mt-0">
                        <DataTable data={invocation[Tab.Fixtures]} testState={testState} highlights={invocation.highlights}/>
                    </TabsContent>

                    <TabsContent value={Tab.CapturedInteractions} className="mt-0">
                        <div className="space-y-1">
                            {invocation[Tab.CapturedInteractions].map((interaction: Interaction) => (
                                <InteractionCard
                                    key={interaction.id}
                                    interaction={interaction}
                                    isPassed={isPassed}
                                    highlights={invocation.highlights}
                                    onExpand={(i: Interaction) => setSelectedInteraction(i)}
                                />
                            ))}
                        </div>
                    </TabsContent>

                    <TabsContent value={Tab.SequenceDiagram} className="mt-0">
                        <SequenceDiagram
                            sequenceDiagram={invocation[Tab.SequenceDiagram] ?? ""}
                            capturedInteractions={invocation[Tab.CapturedInteractions] ?? []}
                            highlights={invocation.highlights}
                            testState={testState}
                        />
                    </TabsContent>

                    {customTabs.map((tab) => (
                        <TabsContent key={tab.id} value={tab.id} className="mt-0">
                            <div className="space-y-1">
                                {customTabCache[tab.file]?.status === 'loading' && (
                                    <div className="text-xs text-muted-foreground">Loading…</div>
                                )}
                                {customTabCache[tab.file]?.status === 'error' && (
                                    <div className="text-xs text-failure">
                                        Failed to load tab content.
                                    </div>
                                )}
                                {customTabCache[tab.file]?.status === 'ready' && (
                                    <div className="-m-4">
                                        <CustomTabPanel
                                            title={tab.label}
                                            content={customTabCache[tab.file]?.content ?? ""}
                                            mediaType={tab.mediaType}
                                            testState={testState}
                                        />
                                    </div>
                                )}
                            </div>
                        </TabsContent>
                    ))}
                </div>
            </ShadcnTabs>

            <InteractionDialog
                interaction={selectedInteraction}
                isOpen={!!selectedInteraction}
                onClose={() => setSelectedInteraction(null)}
                isPassed={isPassed}
                highlights={invocation.highlights}
            />
        </>
    );
};
