import {useState, useEffect, useRef} from 'react';
import {InteractionCard} from './InteractionCard';
import {InteractionDialog} from './InteractionDialog';
import {cn} from "@/lib/utils";
import {Tab} from "@/constants.ts";
import {Interaction, Invocation, NameAndValues} from "@/types/Test.ts";
import {SequenceDiagram} from "@/components/SequenceDiagram.tsx";

type TabValue = typeof Tab[keyof typeof Tab];

interface TabProps {
    invocation: Invocation;
    testState: 'Passed' | 'Failed' | string;
    autoOpenTab?: string;
}

export const Tabs = ({invocation, testState, autoOpenTab}: TabProps) => {
    const isPassed = testState === 'Passed';

    const tabs = [
        {id: Tab.Givens, label: 'Givens', count: invocation[Tab.Givens]?.length},
        {id: Tab.CapturedInteractions, label: 'Interactions', count: invocation[Tab.CapturedInteractions]?.length},
        {id: Tab.CapturedOutputs, label: 'Outputs', count: invocation[Tab.CapturedOutputs]?.length},
        {id: Tab.Fixtures, label: 'Fixtures', count: invocation[Tab.Fixtures]?.length},
        {id: Tab.SequenceDiagram, label: 'Sequence Diagram', exists: !!invocation[Tab.SequenceDiagram]},
    ].filter(t => (t.count && t.count > 0) || t.exists);

    const [selectedInteraction, setSelectedInteraction] = useState<Interaction | null>(null);
    const svgRef = useRef<HTMLDivElement>(null);

    const availableTabIds = tabs.map(t => t.id as TabValue);

    const resolveTab = (incoming?: string): TabValue | undefined => {
        if (!incoming) return undefined;

        if (incoming in Tab) {
            return Tab[incoming as keyof typeof Tab];
        }

        return availableTabIds.find(id => id === incoming);
    };

    const [activeTab, setActiveTab] = useState<TabValue>(() => {
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

    if (tabs.length === 0) return null;

    return (
        <>
            <div className={cn(
                "mt-4 border rounded-lg overflow-hidden bg-card shadow-sm transition-colors",
                isPassed ? "border-emerald-500/20" : "border-rose-500/20"
            )}>
                <div className={cn(
                    "flex border-b overflow-x-auto transition-colors",
                    isPassed ? "bg-emerald-500/5 border-emerald-500/20" : "bg-rose-500/5 border-rose-500/20"
                )}>
                    {tabs.map((tab) => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            className={cn(
                                "px-4 py-2 text-[11px] font-bold tracking-wider transition-all whitespace-nowrap border-b-2",
                                activeTab === tab.id
                                    ? (isPassed
                                        ? "bg-background text-neutral-800 dark:text-neutral-100 border-emerald-500"
                                        : "bg-background text-neutral-800 dark:text-neutral-100 border-rose-500")
                                    : (isPassed
                                        ? "text-muted-foreground border-transparent hover:bg-emerald-500/10 hover:text-emerald-700 dark:hover:text-emerald-400"
                                        : "text-muted-foreground border-transparent hover:bg-rose-500/10 hover:text-rose-700 dark:hover:text-rose-400")
                            )}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                <div className={cn(
                    "p-4 transition-colors",
                    isPassed ? "bg-emerald-50/20 dark:bg-emerald-950/5" : "bg-rose-50/20 dark:bg-rose-950/5"
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

const DataTable = ({data, isPassed}: { data: NameAndValues, isPassed: boolean }) => (
    <table className="w-full text-left border-collapse">
        <thead>
        <tr className="border-b border-border text-[10px] uppercase tracking-widest text-muted-foreground">
            <th className="py-2 font-semibold w-1/3">Name</th>
            <th className="py-2 font-semibold">Value</th>
        </tr>
        </thead>
        <tbody className="divide-y divide-border/40">
        {data.map((row, i) => (
            Object.entries(row).map(([key, val], j) => (
                <tr key={`${i}-${j}`} className={cn(
                    "group transition-colors",
                    isPassed ? "hover:bg-emerald-500/5" : "hover:bg-rose-500/5"
                )}>
                    <td className={cn(
                        "py-2 font-medium align-top text-xs",
                        isPassed ? "text-emerald-900 dark:text-emerald-100" : "text-rose-900 dark:text-rose-100"
                    )}>{key}</td>
                    <td className="py-2 text-muted-foreground font-mono text-[11px] whitespace-pre-wrap">{val}</td>
                </tr>
            ))
        ))}
        </tbody>
    </table>
);