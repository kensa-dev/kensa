import React, {useEffect, useMemo, useRef, useState} from "react";
import type {Interaction} from "@/types/Test.ts";
import {InteractionDialog} from "@/components/InteractionDialog";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Info, Maximize2, X} from "lucide-react";
import "./SequenceDiagram.css";

type TestState = "Passed" | "Failed" | string;

interface SequenceDiagramProps {
    sequenceDiagram: string;
    capturedInteractions: Interaction[];
    testState?: TestState;
    className?: string;
    maxHeight?: number;
}

const getSvgRoot = (host: HTMLElement | null) => host?.querySelector("svg") ?? null;

const forAllCombinations = (items: string[], cb: (a: string, b: string) => void) => {
    for (let i = 0; i < items.length; i += 1) {
        for (let j = i + 1; j < items.length; j += 1) {
            cb(items[i], items[j]);
        }
    }
};

export const SequenceDiagram: React.FC<SequenceDiagramProps> = ({
                                                                    sequenceDiagram,
                                                                    capturedInteractions,
                                                                    testState,
                                                                    className,
                                                                    maxHeight = 700,
                                                                }) => {
    const isPassed = testState === "Passed";

    const scrollRef = useRef<HTMLDivElement>(null);
    const inlineRootRef = useRef<HTMLDivElement>(null);
    const modalRootRef = useRef<HTMLDivElement>(null);

    const [interaction, setInteraction] = useState<Interaction | null>(null);
    const [selectedActors, setSelectedActors] = useState<string[]>([]);
    const [isNoMatchesForFilter, setIsNoMatchesForFilter] = useState(false);
    const [isMaximized, setIsMaximized] = useState(false);
    const [isOverflowing, setIsOverflowing] = useState(false);

    const interactionsById = useMemo(() => {
        const map = new Map<string, Interaction>();
        for (const i of capturedInteractions ?? []) map.set(i.id, i);
        return map;
    }, [capturedInteractions]);

    const annotateMessageGroups = (root: HTMLDivElement | null) => {
        const svg = getSvgRoot(root);
        if (!svg) return;

        // For each clickable label, propagate its interaction id to the parent <g class="message"> group
        svg.querySelectorAll<SVGTextElement>("text.sequence_diagram_clickable").forEach((label) => {
            const interactionId = label.getAttribute("sequence_diagram_interaction_id");
            if (!interactionId) return;

            const messageGroup = label.closest?.("g.message") as SVGGElement | null;
            if (!messageGroup) return;

            messageGroup.setAttribute("data-interaction-id", interactionId);
        });
    };

    const clearHighlights = (root: HTMLDivElement | null) => {
        if (!root) return;
        root.querySelectorAll(".filter-highlight").forEach((el) => el.classList.remove("filter-highlight"));
    };

    const highlightInteractionId = (root: HTMLDivElement | null, interactionId: string) => {
        const svg = getSvgRoot(root);
        if (!svg) return;

        svg
            .querySelectorAll(`[data-interaction-id="${interactionId}"]`)
            .forEach((el) => (el as Element).classList.add("filter-highlight"));
    };

    const highlightActor = (root: HTMLDivElement | null, actorName: string) => {
        const svg = getSvgRoot(root);
        if (!svg) return;

        svg
            .querySelectorAll(
                `.participant[data-participant="${CSS.escape(actorName)}"] rect, .participant[data-participant="${CSS.escape(actorName)}"] text`
            )
            .forEach((el) => (el as Element).classList.add("filter-highlight"));
    };

    const applyFiltering = (root: HTMLDivElement | null) => {
        clearHighlights(root);

        if (selectedActors.length === 0) {
            setIsNoMatchesForFilter(false);
            return;
        }

        const matchingIds: string[] = [];

        if (selectedActors.length === 1) {
            const actor = selectedActors[0];
            interactionsById.forEach((i, id) => {
                if (i.from === actor || i.to === actor) matchingIds.push(id);
            });
        } else {
            forAllCombinations(selectedActors, (a, b) => {
                interactionsById.forEach((i, id) => {
                    const isAB = i.from === a && i.to === b;
                    const isBA = i.from === b && i.to === a;
                    if (isAB || isBA) matchingIds.push(id);
                });
            });
        }

        selectedActors.forEach((a) => highlightActor(root, a));
        matchingIds.forEach((id) => highlightInteractionId(root, id));
        setIsNoMatchesForFilter(matchingIds.length === 0);
    };

    const attachClickHandler = (root: HTMLDivElement | null) => {
        if (!root) return () => {
        };

        const onClickCapture = (e: MouseEvent) => {
            const target = e.target as Element | null;
            if (!target) return;

            const clickable = target.closest?.(".sequence_diagram_clickable") as Element | null;
            if (clickable) {
                const interactionId =
                    target.getAttribute("sequence_diagram_interaction_id") ??
                    clickable.getAttribute("sequence_diagram_interaction_id");

                if (interactionId) {
                    const found = interactionsById.get(interactionId) ?? null;
                    if (found) {
                        e.preventDefault();
                        e.stopPropagation();
                        setInteraction(found);
                        return;
                    }
                }
            }

            const participant = target.closest?.(".participant[data-participant]") as Element | null;
            const actorName = participant?.getAttribute("data-participant");
            if (actorName) {
                e.preventDefault();
                e.stopPropagation();
                setSelectedActors((prev) =>
                    prev.includes(actorName) ? prev.filter((n) => n !== actorName) : prev.concat(actorName)
                );
            }
        };

        root.addEventListener("click", onClickCapture, true);
        return () => root.removeEventListener("click", onClickCapture, true);
    };

    // Detect overflow to decide if we show the expand button
    useEffect(() => {
        const scroller = scrollRef.current;
        if (!scroller) return;

        const compute = () => {
            const hasX = scroller.scrollWidth > scroller.clientWidth + 1;
            const hasY = scroller.scrollHeight > scroller.clientHeight + 1;
            setIsOverflowing(hasX || hasY);
        };

        compute();

        const ro = new ResizeObserver(() => compute());
        ro.observe(scroller);

        // The SVG may not exist at effect time; try to attach once it’s injected
        let rafId = 0;
        const tryObserveSvg = () => {
            const svg = scroller.querySelector("svg");
            if (svg) {
                ro.observe(svg);
                compute();
                return;
            }
            rafId = window.requestAnimationFrame(tryObserveSvg);
        };
        rafId = window.requestAnimationFrame(tryObserveSvg);

        return () => {
            window.cancelAnimationFrame(rafId);
            ro.disconnect();
        };
    }, [sequenceDiagram, maxHeight]);

    // Inline init + click binding after injection (rebinding on sequenceDiagram changes)
    useEffect(() => {
        const raf = window.requestAnimationFrame(() => {
            annotateMessageGroups(inlineRootRef.current);
            applyFiltering(inlineRootRef.current);
        });

        const detach = attachClickHandler(inlineRootRef.current);

        return () => {
            window.cancelAnimationFrame(raf);
            detach();
        };
    }, [sequenceDiagram, interactionsById]);

    // Re-apply filtering when selection changes
    useEffect(() => {
        applyFiltering(inlineRootRef.current);
        applyFiltering(modalRootRef.current);
    }, [selectedActors, interactionsById]);

    useEffect(() => {
        if (!isMaximized) return;

        let cancelled = false;
        let rafId = 0;
        let detach = () => {};

        const tryInit = () => {
            if (cancelled) return;

            const root = modalRootRef.current;
            if (!root) {
                rafId = window.requestAnimationFrame(tryInit);
                return;
            }

            annotateMessageGroups(root);
            applyFiltering(root);
            detach = attachClickHandler(root);
        };

        rafId = window.requestAnimationFrame(tryInit);

        return () => {
            cancelled = true;
            window.cancelAnimationFrame(rafId);
            detach();
        };
    }, [isMaximized, sequenceDiagram, interactionsById]);

    if (!sequenceDiagram) return null;

    const rootClasses =
        "sequence-diagram" +
        (selectedActors.length > 0 ? " filtered" : "") +
        (isNoMatchesForFilter ? " no-matches" : "");

    return (
        <>
            <div className={["sd-panel", className].filter(Boolean).join(" ")}>
                {isOverflowing && (
                    <button
                        type="button"
                        className="sd-expand"
                        onClick={() => setIsMaximized(true)}
                        title="Expand diagram"
                    >
                        <Maximize2 size={14}/>
                    </button>
                )}

                <div
                    ref={scrollRef}
                    className="bg-white sd-scroll"
                    style={{ maxHeight, ["--sd-bg" as any]: isPassed ? "rgba(16, 185, 129, 0.06)" : "rgba(244, 63, 94, 0.06)" }}
                >
                    <div
                        ref={inlineRootRef}
                        className={rootClasses}
                        dangerouslySetInnerHTML={{__html: sequenceDiagram}}
                    />
                </div>
            </div>

            <Dialog open={isMaximized} onOpenChange={setIsMaximized}>
                <DialogContent
                    className={"fixed flex flex-col gap-0 p-0 overflow-hidden outline-none [&>button]:hidden border-none bg-background max-w-none " +
                        "left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 " +
                        "w-[calc(100vw-2rem)] h-[calc(100vh-2rem)] rounded-2xl ring-1 ring-border/50 shadow-none"
                    }
                >
                    <div className="px-4 h-12 flex flex-row justify-between border-b shrink-0 bg-background">
                        <div className="px-4 py-1.5 border-b border-border/20 flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-muted-foreground/70 shrink-0">
                            <Info size={12} /> Sequence Diagram
                        </div>
                        <button
                            type="button"
                            onClick={() => setIsMaximized(false)}
                            className="p-2 hover:bg-destructive/10 hover:text-destructive rounded-md text-muted-foreground transition-colors focus:outline-none"
                            title="Close"
                        >
                            <X size={18}/>
                        </button>
                    </div>

                    <div className="bg-white flex-1 overflow-auto p-6 flex justify-center">
                        <div
                            ref={modalRootRef}
                            className={rootClasses}
                            dangerouslySetInnerHTML={{__html: sequenceDiagram}}
                        />
                    </div>
                </DialogContent>
            </Dialog>
            <InteractionDialog
                interaction={interaction}
                isOpen={!!interaction}
                onClose={() => setInteraction(null)}
                isPassed={isPassed}
            />
        </>
    );
};