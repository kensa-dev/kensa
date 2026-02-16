import {useEffect, useRef, useState} from 'react';
import {InteractionCard} from './InteractionCard';
import {InteractionDialog} from './InteractionDialog';
import {cn} from "@/lib/utils";
import {Tab} from "@/constants.ts";
import {Interaction, Invocation} from "@/types/Test.ts";
import {SequenceDiagram} from "@/components/SequenceDiagram.tsx";
import {loadText} from "@/lib/utils.ts";
import {CustomTabPanel} from "@/components/CustomTabPanel.tsx";
import {DataTable} from "@/components/DataTable.tsx";

type TabValue = typeof Tab[keyof typeof Tab];

interface TabProps {
    invocation: Invocation;
    testState: 'Passed' | 'Failed' | string;
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

    return (
        <>
            <div className={cn(
                "border rounded-lg overflow-hidden bg-card shadow-sm transition-colors",
                isPassed ? "border-success-30" : "border-failure-30"
            )}>
                <div className={cn(
                    "flex border-b overflow-x-auto transition-colors",
                    isPassed ? "bg-success-10 border-success-30" : "bg-failure-10 border-failure-30"
                )}>
                    {tabs.map((tab) => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            className={cn(
                                "px-4 py-2 text-[11px] font-bold tracking-wider transition-all whitespace-nowrap border-b-2",
                                activeTab === tab.id
                                    ? (isPassed
                                        ? "bg-background text-neutral-800 dark:text-neutral-100 border-success-30"
                                        : "bg-background text-neutral-800 dark:text-neutral-100 border-failure-30")
                                    : (isPassed
                                        ? "text-muted-foreground border-transparent hover:bg-success-10 hover:text-success"
                                        : "text-muted-foreground border-transparent hover:bg-failure-10 hover:text-failure")
                            )}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                <div className={cn(
                    "p-4 transition-colors",
                    isPassed ? "bg-success-2 dark:bg-success-10" : "bg-failure-2 dark:bg-failure-10"
                )}>
                    {activeTab === Tab.Givens && <DataTable data={invocation[Tab.Givens]} isPassed={isPassed}/>}
                    {activeTab === Tab.CapturedOutputs && <DataTable data={invocation[Tab.CapturedOutputs]} isPassed={isPassed}/>}
                    {activeTab === Tab.Fixtures && <DataTable data={invocation[Tab.Fixtures]} isPassed={isPassed}/>}

                    {activeTab === Tab.CapturedInteractions && (
                        <div className="space-y-1">
                            {invocation[Tab.CapturedInteractions].map((interaction: Interaction) => (
                                <InteractionCard
                                    key={interaction.id}
                                    interaction={interaction}
                                    isPassed={isPassed}
                                    onExpand={(i: Interaction) => setSelectedInteraction(i)}
                                />
                            ))}
                        </div>
                    )}

                    {(activeTab === Tab.SequenceDiagram) && (
                        <SequenceDiagram
                            sequenceDiagram={invocation[Tab.SequenceDiagram] ?? ""}
                            capturedInteractions={invocation[Tab.CapturedInteractions] ?? []}
                            testState={testState}
                        />
                    )}

                    {activeCustomTab && (
                        <div className="space-y-1">
                            {activeCustomFile && customTabCache[activeCustomFile]?.status === 'loading' && (
                                <div className="text-xs text-muted-foreground">Loading…</div>
                            )}
                            {activeCustomFile && customTabCache[activeCustomFile]?.status === 'error' && (
                                <div className="text-xs text-failure dark:text-failure">
                                    Failed to load tab content.
                                </div>
                            )}
                            {activeCustomFile && customTabCache[activeCustomFile]?.status === 'ready' && (
                                <div className="-m-4">
                                    <CustomTabPanel
                                        title={activeCustomLabel}
                                        content={customTabCache[activeCustomFile]?.content ?? ""}
                                        testState={testState}
                                    />
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
            <InteractionDialog
                interaction={selectedInteraction}
                isOpen={!!selectedInteraction}
                onClose={() => setSelectedInteraction(null)}
                isPassed={isPassed}
            />
        </>
    );
};
