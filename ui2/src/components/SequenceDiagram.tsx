import React, { useEffect, useMemo, useRef, useState } from "react";
import type { Interaction } from "@/types/Test.ts";
import { InteractionDialog } from "@/components/InteractionDialog";
import "./SequenceDiagram.css";

type TestState = "Passed" | "Failed" | string;

interface SequenceDiagramProps {
  sequenceDiagram: string;
  capturedInteractions: Interaction[];
  testState?: TestState;
  className?: string;
}

const getSvgRoot = (host: HTMLElement | null) => host?.querySelector("svg") ?? null;

const forAllCombinations = (items: string[], cb: (a: string, b: string) => void) => {
  for (let i = 0; i < items.length; i += 1) {
    for (let j = i + 1; j < items.length; j += 1) {
      cb(items[i], items[j]);
    }
  }
};

const actorChildrenOf = (svg: ParentNode): Element[] =>
  Array.from(svg.querySelectorAll("text:not(.sequence_diagram_clickable)"))
    .filter((textNode) => {
      const prev = (textNode as Element).previousElementSibling as Element | null;
      return !!(
        prev &&
        prev.tagName === "rect" &&
        prev.previousElementSibling &&
        prev.previousElementSibling.tagName !== "path"
      );
    })
    .flatMap((textNode) => {
      const rect = (textNode as Element).previousElementSibling as Element | null;
      return rect ? [textNode as Element, rect] : [textNode as Element];
    });

const nameForActor = (textOrRectNode: Element) => {
  const attr = textOrRectNode.getAttribute("data-actor");
  if (attr) return attr;

  const textNode =
    textOrRectNode.tagName === "text"
      ? textOrRectNode
      : (textOrRectNode.nextElementSibling as Element | null);

  return (textNode?.textContent ?? "").trim();
};

export const SequenceDiagram: React.FC<SequenceDiagramProps> = ({
  sequenceDiagram,
  capturedInteractions,
  testState,
  className,
}) => {
  const isPassed = testState === "Passed";
  const rootRef = useRef<HTMLDivElement>(null);

  const [interaction, setInteraction] = useState<Interaction | null>(null);
  const [selectedActorNames, setSelectedActorNames] = useState<string[]>([]);
  const [isNoMatchesForFilter, setIsNoMatchesForFilter] = useState(false);

  const interactionsById = useMemo(() => {
    const map = new Map<string, Interaction>();
    for (const i of capturedInteractions ?? []) map.set(i.id, i);
    return map;
  }, [capturedInteractions]);

  const annotateActors = () => {
    const root = rootRef.current;
    const svg = getSvgRoot(root);
    if (!svg) return;

    actorChildrenOf(svg).forEach((node) => {
      const actorName = nameForActor(node);
      if (!actorName) return;

      node.classList.add("actor", `actor-${actorName}`);
      node.setAttribute("data-actor", actorName);
    });
  };

  const clearHighlights = () => {
    const root = rootRef.current;
    if (!root) return;
    root.querySelectorAll(".filter-highlight").forEach((el) => el.classList.remove("filter-highlight"));
  };

  const highlightInteractionId = (interactionId: string) => {
    const root = rootRef.current;
    const svg = getSvgRoot(root);
    if (!svg) return;

    svg
      .querySelectorAll(`[sequence_diagram_interaction_id="${interactionId}"]`)
      .forEach((el) => (el as Element).classList.add("filter-highlight"));
  };

  const highlightActor = (actorName: string) => {
    const root = rootRef.current;
    if (!root) return;

    root.querySelectorAll(`.actor-${actorName}`).forEach((el) => el.classList.add("filter-highlight"));
  };

  const applyFiltering = () => {
    const root = rootRef.current;
    if (!root) return;

    clearHighlights();

    if (selectedActorNames.length === 0) {
      setIsNoMatchesForFilter(false);
      return;
    }

    const selected = selectedActorNames;

    const matchingIds: string[] = [];

    if (selected.length === 1) {
      const actor = selected[0];
      interactionsById.forEach((i, id) => {
        if (i.from === actor || i.to === actor) matchingIds.push(id);
      });
    } else {
      forAllCombinations(selected, (a, b) => {
        interactionsById.forEach((i, id) => {
          const isAB = i.from === a && i.to === b;
          const isBA = i.from === b && i.to === a;
          if (isAB || isBA) matchingIds.push(id);
        });
      });
    }

    selected.forEach(highlightActor);
    matchingIds.forEach(highlightInteractionId);

    setIsNoMatchesForFilter(matchingIds.length === 0);
  };

  useEffect(() => {
    annotateActors();
    applyFiltering();
  }, [sequenceDiagram]);

  useEffect(() => {
    applyFiltering();
  }, [selectedActorNames, interactionsById]);

  useEffect(() => {
    const root = rootRef.current;
    if (!root) return;

    const onClickCapture = (e: MouseEvent) => {
      const target = e.target as Element | null;
      if (!target) return;

      // 1) Clickable interaction
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

      const actorEl = target.closest?.("[data-actor]") as Element | null;
      const actorName = actorEl?.getAttribute("data-actor");
      if (actorName) {
        e.preventDefault();
        e.stopPropagation();
        setSelectedActorNames((prev) =>
          prev.includes(actorName) ? prev.filter((n) => n !== actorName) : prev.concat(actorName)
        );
      }
    };

    root.addEventListener("click", onClickCapture, true);
    return () => root.removeEventListener("click", onClickCapture, true);
  }, [interactionsById]);

  if (!sequenceDiagram) return null;

  const rootClasses =
    "sequence-diagram" +
    (selectedActorNames.length > 0 ? " filtered" : "") +
    (isNoMatchesForFilter ? " no-matches" : "");

  return (
    <>
      <div
        ref={rootRef}
        className={[rootClasses, className].filter(Boolean).join(" ")}
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