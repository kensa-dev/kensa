import "./SequenceDiagram.css";
import React, { useEffect, useMemo, useRef, useState } from "react";
import type { Interaction } from "@/types/Test.ts";
import { InteractionDialog } from "@/components/InteractionDialog";

type TestState = "Passed" | "Failed" | string;

interface SequenceDiagramProps {
    sequenceDiagram: string;
    capturedInteractions: Interaction[];
    testState?: TestState;
    className?: string;
}

export const SequenceDiagram: React.FC<SequenceDiagramProps> = ({
                                                                    sequenceDiagram,
                                                                    capturedInteractions,
                                                                    testState,
                                                                    className,
                                                                }) => {
    const isPassed = testState === "Passed";
    const rootRef = useRef<HTMLDivElement>(null);
    const [interaction, setInteraction] = useState<Interaction | null>(null);

    const interactionsById = useMemo(() => {
        const map = new Map<string, Interaction>();
        for (const i of capturedInteractions ?? []) map.set(i.id, i);
        return map;
    }, [capturedInteractions]);

    useEffect(() => {
        const root = rootRef.current;
        if (!root) return;

        const onClickCapture = (e: MouseEvent) => {
            const target = e.target as Element | null;
            if (!target) return;

            const clickable = target.closest?.(".sequence_diagram_clickable") as Element | null;
            if (!clickable) return;

            const interactionId =
                target.getAttribute("sequence_diagram_interaction_id") ??
                clickable.getAttribute("sequence_diagram_interaction_id");

            if (!interactionId) return;

            const found = interactionsById.get(interactionId) ?? null;
            if (!found) return;

            e.preventDefault();
            e.stopPropagation();

            setInteraction(found);
        };

        root.addEventListener("click", onClickCapture, true);
        return () => root.removeEventListener("click", onClickCapture, true);
    }, [interactionsById]);

    if (!sequenceDiagram) return null;

    return (
        <>
            <div
                ref={rootRef}
                className={className}
                dangerouslySetInnerHTML={{ __html: sequenceDiagram }}
            />

            <InteractionDialog
                interaction={interaction}
                isOpen={!!interaction}
                onClose={() => setInteraction(null)}
                isPassed={isPassed}
            />
        </>
    );
};