import React, { useEffect, useRef, useState } from "react";
import { Info, Maximize2, X } from "lucide-react";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import "./SequenceDiagram.css";
import "./ComponentDiagram.css";

interface ComponentDiagramProps {
    svg: string;
    className?: string;
    maxHeight?: number;
}

export const ComponentDiagram: React.FC<ComponentDiagramProps> = ({ svg, className, maxHeight = 700 }) => {
    const scrollRef = useRef<HTMLDivElement>(null);
    const [isMaximized, setIsMaximized] = useState(false);
    const [isOverflowing, setIsOverflowing] = useState(false);

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

        let rafId = 0;
        const tryObserveSvg = () => {
            const svgEl = scroller.querySelector("svg");
            if (svgEl) {
                ro.observe(svgEl);
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
    }, [svg, maxHeight]);

    if (!svg) return null;

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
                        <Maximize2 size={14} />
                    </button>
                )}

                <div
                    ref={scrollRef}
                    className="sd-scroll"
                    style={{ maxHeight } as React.CSSProperties}
                >
                    <div
                        className="component-diagram"
                        dangerouslySetInnerHTML={{ __html: svg }}
                    />
                </div>
            </div>

            <Dialog open={isMaximized} onOpenChange={setIsMaximized}>
                <DialogContent
                    className={
                        "fixed flex flex-col gap-0 p-0 overflow-hidden outline-none [&>button]:hidden border-none bg-background max-w-none " +
                        "left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 " +
                        "w-[calc(100vw-2rem)] h-[calc(100vh-2rem)] rounded-2xl ring-1 ring-border/50 shadow-none"
                    }
                >
                    <div className="px-4 h-12 flex flex-row justify-between border-b shrink-0 bg-background">
                        <div className="px-4 py-1.5 border-b border-border/20 flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-muted-foreground/90 shrink-0">
                            <Info size={12} /> Component Diagram
                        </div>
                        <button
                            type="button"
                            onClick={() => setIsMaximized(false)}
                            className="p-2 hover:bg-muted hover:text-foreground rounded-md text-muted-foreground transition-colors focus:outline-none"
                            title="Close"
                        >
                            <X size={18} />
                        </button>
                    </div>

                    <div className="flex-1 overflow-auto p-6 flex justify-center bg-background/50">
                        <div
                            className="sequence-diagram"
                            dangerouslySetInnerHTML={{ __html: svg }}
                        />
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
};
