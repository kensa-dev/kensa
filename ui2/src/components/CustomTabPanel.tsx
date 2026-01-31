import * as React from "react";
import {ChevronDown, ChevronUp, Copy, Info, Maximize2, Search, WrapText, X} from "lucide-react";
import hljs from "highlight.js";
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {cn} from "@/lib/utils.ts";

type TestState = "Passed" | "Failed" | string;

interface CustomTabPanelProps {
    title: string;
    content: string;
    testState?: TestState;
    language?: string;
    maxHeight?: number;
}

const getAllTextNodes = (root: Node): Text[] => {
    const textNodes: Text[] = [];
    const stack = [root];
    while (stack.length) {
        const node = stack.pop();
        if (node?.nodeType === Node.TEXT_NODE) {
            textNodes.push(node as Text);
        } else if (node?.childNodes) {
            for (let i = node.childNodes.length - 1; i >= 0; i--) {
                stack.push(node.childNodes[i]);
            }
        }
    }
    return textNodes;
};

const removeHighlights = (root: HTMLElement) => {
    const marks = root.querySelectorAll(".search-highlight");
    marks.forEach((mark) => {
        const parent = mark.parentNode;
        if (!parent) return;
        while (mark.firstChild) {
            parent.insertBefore(mark.firstChild, mark);
        }
        parent.removeChild(mark);
        parent.normalize();
    });
};

const highlightHtml = (content: string, language?: string): string => {
    const lang = (language ?? "").toLowerCase().trim();
    if (!content) return "";

    try {
        if (!lang || lang === "plaintext" || lang === "plain" || lang === "text") {
            return hljs.highlightAuto(content).value;
        }
        return hljs.highlight(content, {language: lang}).value;
    } catch {
        return hljs.highlightAuto(content).value;
    }
};

export const CustomTabPanel: React.FC<CustomTabPanelProps> = ({
                                                                  title,
                                                                  content,
                                                                  testState,
                                                                  language = "plaintext",
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

    const highlighted = React.useMemo(() => highlightHtml(content, language), [content, language]);

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
                        mark.className =
                            "search-highlight px-0.5 rounded-sm bg-yellow-300 text-black transition-all duration-200";
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
            const activeIdx = ((currentIndex % allMatchElements.length) + allMatchElements.length) % allMatchElements.length;
            const activeMark = allMatchElements[activeIdx];
            if (activeMark) {
                activeMark.className = cn(
                    "search-highlight px-0.5 rounded-sm transition-all duration-200",
                    "bg-orange-500 text-white shadow-[0_0_10px_rgba(249,115,22,0.8)] ring-1 ring-orange-300 z-10 scale-110"
                );
                activeMark.scrollIntoView({block: "center", inline: "nearest", behavior: "smooth"});
            }
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

    const nextMatch = () => { if (matchCount > 0) setCurrentIndex((prev) => (prev + 1) % matchCount); };
    const prevMatch = () => { if (matchCount > 0) setCurrentIndex((prev) => (prev - 1 + matchCount) % matchCount); };

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
                    onChange={(e) => { setSearchQuery(e.target.value); setCurrentIndex(0); }}
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
                    className={cn("p-1.5 rounded-md transition-all flex items-center justify-center text-muted-foreground hover:text-primary")}
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
                    {(language || "text").toLowerCase()}
                </span>
            </div>
        </div>
    );

    const contentArea = (codeRef: React.RefObject<HTMLElement>, maxH?: number) => (
        <pre
            className={cn(
                "flex-1 overflow-auto p-6 text-[13px] font-mono leading-relaxed selection:bg-primary/20 custom-scrollbar transition-all bg-transparent",
                isWrapped ? "whitespace-pre-wrap" : "whitespace-pre"
            )}
            style={maxH ? {maxHeight: maxH} : undefined}
        >
            <code ref={codeRef} className="hljs" dangerouslySetInnerHTML={{__html: highlighted}}/>
        </pre>
    );

    return (
        <>
            <div className={cn("flex flex-col bg-background overflow-hidden", isPassed ? "bg-emerald-500/[0.02]" : "bg-rose-500/[0.02]")}>
                {toolbar}
                <div className="flex-1 flex flex-col overflow-hidden bg-background">
                    {contentArea(inlineCodeRef, maxHeight)}
                </div>
            </div>

            <Dialog open={isMaximized} onOpenChange={setIsMaximized}>
                <DialogContent
                    onEscapeKeyDown={(e) => {
                        const activeElement = document.activeElement;
                        if (activeElement instanceof HTMLInputElement && activeElement.value !== "") {
                            e.preventDefault();
                            return;
                        }
                        if (isMaximized) {
                            e.preventDefault();
                            setIsMaximized(false);
                        }
                    }}
                    className={cn(
                        "fixed flex flex-col gap-0 p-0 overflow-hidden transition-all duration-500 ease-in-out outline-none shadow-2xl [&>button]:hidden border-none bg-background max-w-none",
                        "left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2",
                        "w-[calc(100vw-2rem)] h-[calc(100vh-2rem)] rounded-2xl ring-1 ring-border/50 shadow-none"
                    )}
                >
                    <div className={cn("flex-1 overflow-hidden transition-all duration-500", "p-0")}>
                        <div className="p-0">
                            <div className="flex flex-col h-full bg-background overflow-hidden">
                                {toolbar}
                                <div className="flex-1 overflow-hidden bg-background">
                                    {contentArea(modalCodeRef)}
                                </div>
                            </div>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
};