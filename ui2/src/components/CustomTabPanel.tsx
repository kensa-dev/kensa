import * as React from "react";
import {ChevronDown, ChevronUp, Copy, Maximize2, Search, WrapText, X} from "lucide-react";
import hljs from "highlight.js";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {cn, getAllTextNodes, removeHighlights} from "@/lib/utils.ts";
import {TestState} from "@/types/Test.ts";

interface CustomTabPanelProps {
    title: string;
    content: string;
    testState?: TestState;
    maxHeight?: number;
}

const highlightHtml = (content: string): string => hljs.highlight(content, {language: "plaintext"}).value;

export const CustomTabPanel: React.FC<CustomTabPanelProps> = ({
                                                                  content,
                                                                  testState,
                                                                  maxHeight = 700,
                                                              }) => {
    const isPassed = testState === "Passed";

    const [isMaximized, setIsMaximized] = React.useState(false);
    const [searchQuery, setSearchQuery] = React.useState("");
    const [currentIndex, setCurrentIndex] = React.useState(0);
    const [isWrapped, setIsWrapped] = React.useState(true);
    const [matchCount, setMatchCount] = React.useState(0);

    const inlineCodeRef = React.useRef<HTMLElement>(null);
    const modalCodeRef = React.useRef<HTMLElement>(null);

    const highlighted = React.useMemo(() => highlightHtml(content), [content]);

    const applySearchTo = React.useCallback((root: HTMLElement | null) => {
        if (!root) return;

        removeHighlights(root);

        if (!searchQuery || searchQuery.length < 2) {
            setMatchCount(0);
            setCurrentIndex(0);
            return;
        }

        const textNodes = getAllTextNodes(root);
        const lowerQuery = searchQuery.toLowerCase();
        const allMatchElements: HTMLElement[] = [];

        textNodes.forEach((node) => {
            const text = node.data.toLowerCase();
            const matchOffsets: number[] = [];
            let start = 0;

            while ((start = text.indexOf(lowerQuery, start)) !== -1) {
                matchOffsets.push(start);
                start += lowerQuery.length;
            }

            const nodeMatchElements: HTMLElement[] = [];
            for (let i = matchOffsets.length - 1; i >= 0; i--) {
                const offset = matchOffsets[i];
                if (offset + searchQuery.length <= node.length) {
                    try {
                        const mid = node.splitText(offset);
                        mid.splitText(searchQuery.length);

                        const mark = document.createElement("span");
                        mark.className = "search-highlight";
                        mid.parentNode?.replaceChild(mark, mid);
                        mark.appendChild(mid);
                        nodeMatchElements.unshift(mark);
                    } catch {
                        // ignore
                    }
                }
            }

            allMatchElements.push(...nodeMatchElements);
        });

        if (allMatchElements.length > 0) {
            const activeIdx =
                ((currentIndex % allMatchElements.length) + allMatchElements.length) % allMatchElements.length;

            allMatchElements.forEach((el, idx) => {
                if (idx === activeIdx) {
                    el.classList.add("search-highlight-active");
                    el.scrollIntoView({block: "center", inline: "nearest", behavior: "smooth"});
                } else {
                    el.classList.remove("search-highlight-active");
                }
            });
        }

        setMatchCount(allMatchElements.length);
    }, [currentIndex, searchQuery]);

    React.useLayoutEffect(() => {
        applySearchTo(inlineCodeRef.current);
    }, [applySearchTo, highlighted, isWrapped]);

    React.useLayoutEffect(() => {
        if (!isMaximized) return;
        applySearchTo(modalCodeRef.current);
    }, [applySearchTo, highlighted, isWrapped, isMaximized]);

    React.useEffect(() => {
        if (!isMaximized) return;
        const raf = window.requestAnimationFrame(() => applySearchTo(modalCodeRef.current));
        return () => window.cancelAnimationFrame(raf);
    }, [isMaximized, applySearchTo]);

    const nextMatch = () => {
        if (matchCount > 0) setCurrentIndex((prev) => (prev + 1) % matchCount);
    };
    const prevMatch = () => {
        if (matchCount > 0) setCurrentIndex((prev) => (prev - 1 + matchCount) % matchCount);
    };

    const onCopy = () => {
        if (!content) return;
        navigator.clipboard.writeText(content);
    };

    if (!content) return null;

    const toolbar = (
        <div className="bg-muted/40 px-3 py-1.5 border-b border-border/50 flex justify-between items-center shrink-0 gap-4">
            <div className="flex items-center bg-background border border-border rounded-md px-2 py-0.5 gap-2 grow max-w-md mx-auto group focus-within:ring-1 focus-within:ring-primary transition-all">
                <Search size={12} className="text-muted-foreground group-focus-within:text-primary"/>
                <input
                    value={searchQuery}
                    onChange={(e) => {
                        setSearchQuery(e.target.value);
                        setCurrentIndex(0);
                    }}
                    placeholder="Find..."
                    className="bg-transparent border-none outline-none text-[11px] text-foreground placeholder:text-muted-foreground w-full"
                    onKeyDown={(e) => {
                        if (e.key === "Enter") e.shiftKey ? prevMatch() : nextMatch();
                        if (e.key === "Escape" && searchQuery !== "") {
                            setSearchQuery("");
                            e.stopPropagation();
                        }
                    }}
                />
                {searchQuery && (
                    <div className="flex items-center gap-1 shrink-0">
                        <span className="text-[9px] font-mono text-muted-foreground px-1 border-r border-border mr-1">
                            {matchCount > 0 ? `${Math.min(currentIndex + 1, matchCount)}/${matchCount}` : "0/0"}
                        </span>
                        <button onClick={prevMatch} className="text-muted-foreground hover:text-foreground p-0.5"><ChevronUp size={12}/></button>
                        <button onClick={nextMatch} className="text-muted-foreground hover:text-foreground p-0.5"><ChevronDown size={12}/></button>
                        <button onClick={() => setSearchQuery("")} className="text-muted-foreground hover:text-destructive p-0.5 ml-1"><X size={12}/></button>
                    </div>
                )}
            </div>

            <div className="flex items-center gap-1 shrink-0">
                <button
                    onClick={() => setIsMaximized(!isMaximized)}
                    className={"p-1.5 rounded-md transition-all flex items-center justify-center text-muted-foreground hover:text-primary"}
                    title={isMaximized ? "Exit Fullscreen" : "Maximize"}
                >
                    {isMaximized ? <X size={16}/> : <Maximize2 size={14}/>}
                </button>

                <button
                    onClick={() => setIsWrapped(!isWrapped)}
                    className={cn(
                        "p-1.5 rounded transition-colors",
                        isWrapped ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground"
                    )}
                    title="Toggle Word Wrap"
                >
                    <WrapText size={14}/>
                </button>

                <button onClick={onCopy} className="p-1.5 text-muted-foreground hover:text-foreground" title="Copy">
                    <Copy size={14}/>
                </button>

                <span className="text-[9px] font-bold text-primary uppercase bg-primary/10 px-1.5 py-0.5 rounded border border-primary/20 ml-1">
                    {"plaintext"}
                </span>
            </div>
        </div>
    );

    const contentArea = (codeRef: React.RefObject<HTMLElement>) => (
        <pre
            className={cn(
                "p-6 text-[13px] font-mono leading-relaxed selection:bg-primary/20 transition-all bg-transparent",
                isWrapped ? "whitespace-pre-wrap" : "whitespace-pre"
            )}
        >
        <code ref={codeRef} className="hljs" dangerouslySetInnerHTML={{__html: highlighted}}/>
    </pre>
    );

    const inlineContent = (
        <div className="flex-1 overflow-auto custom-scrollbar" style={{maxHeight}}>
            {contentArea(inlineCodeRef)}
        </div>
    );

    const modalContent = (
        <div className="flex flex-col h-full overflow-hidden">
            <div className="shrink-0 sticky top-0 z-10">
                {toolbar}
            </div>
            <div className="flex-1 overflow-auto custom-scrollbar">
                {contentArea(modalCodeRef)}
            </div>
        </div>
    );

    return (
        <>
            <div className={"flex flex-col overflow-hidden bg-white dark:bg-background"}>
                <div className={cn(
                    "shrink-0",
                    isPassed ? "bg-success-10" : "bg-failure-10"
                )}>
                    {toolbar}
                </div>
                {inlineContent}
            </div>

            <Dialog open={isMaximized} onOpenChange={setIsMaximized}>
                <DialogContent
                    className={
                        "fixed flex flex-col gap-0 p-0 overflow-hidden outline-none shadow-2xl [&>button]:hidden border-none bg-white dark:bg-background max-w-none " +
                        "left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[calc(100vw-2rem)] h-[calc(100vh-2rem)] rounded-2xl ring-1 ring-border/50 shadow-none"
                    }
                >
                    {modalContent}
                </DialogContent>
            </Dialog>
        </>
    );
};