import * as React from 'react';
import { FileJson, Info, ChevronUp, ChevronDown, Copy, Search, X, Maximize2, ExternalLink, WrapText } from 'lucide-react';
import hljs from 'highlight.js';
import json from 'highlight.js/lib/languages/json';
import xml from 'highlight.js/lib/languages/xml';
import { cn, getAllTextNodes, removeHighlights } from "@/lib/utils";

hljs.registerLanguage('json', json);
hljs.registerLanguage('xml', xml);

export const InteractionContent = ({ interaction, isPassed, hideMetadata, onExpand, isMaximized, onToggleMaximize }: any) => {
    const { rendered } = interaction;
    const [searchQuery, setSearchQuery] = React.useState("");
    const [currentIndex, setCurrentIndex] = React.useState(0);
    const [isWrapped, setIsWrapped] = React.useState(false);
    const codeRef = React.useRef<HTMLElement>(null);

    const payloads = rendered.values || [];
    const metadataGroups = rendered.attributes || [];
    const hasValidPayload = React.useMemo(() => payloads.some((p: any) => p.value && p.value.trim() !== ""), [payloads]);

    const [matchCount, setMatchCount] = React.useState(0);

    React.useLayoutEffect(() => {
        const root = codeRef.current;
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

        textNodes.forEach(node => {
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
                        const mark = document.createElement('span');
                        mark.className = "search-highlight px-0.5 rounded-sm bg-yellow-300 text-black transition-all duration-200";
                        mid.parentNode?.replaceChild(mark, mid);
                        mark.appendChild(mid);
                        nodeMatchElements.unshift(mark);
                    } catch (e) { /* ignore split errors */ }
                }
            }
            allMatchElements.push(...nodeMatchElements);
        });

        if (allMatchElements.length > 0) {
            const activeIdx = currentIndex % allMatchElements.length;
            const activeMark = allMatchElements[activeIdx];
            if (activeMark) {
                activeMark.className = cn(
                    "search-highlight px-0.5 rounded-sm transition-all duration-200",
                    "bg-orange-500 text-white shadow-[0_0_10px_rgba(249,115,22,0.8)] ring-1 ring-orange-300 z-10 scale-110"
                );
                activeMark.scrollIntoView({ block: 'center', inline: 'nearest', behavior: 'smooth' });
            }
        }

        setMatchCount(allMatchElements.length);
    }, [searchQuery, currentIndex, isWrapped, interaction]);

    const onCopy = () => { if (payloads[0]?.value) navigator.clipboard.writeText(payloads[0].value); };
    const nextMatch = () => { if (matchCount > 0) setCurrentIndex(prev => (prev + 1) % matchCount); };
    const prevMatch = () => { if (matchCount > 0) setCurrentIndex(prev => (prev - 1 + matchCount) % matchCount); };

    return (
        <div className={cn(
            "flex flex-col h-full bg-background relative font-sans transition-all duration-500",
            isMaximized ? "border-none shadow-none rounded-none" : "border rounded-xl border-border/60 shadow-sm"
        )}>

            {/* Metadata Dashboard */}
            {!hideMetadata && metadataGroups.length > 0 && (
                <div className={cn(
                    "flex flex-col border-b overflow-hidden transition-all duration-300 bg-background",
                    hasValidPayload ? "shrink-0 max-h-[40%]" : "flex-1",
                    isPassed ? "bg-emerald-500/[0.02]" : "bg-rose-500/[0.02]"
                )}>
                    <div className="px-4 py-1.5 border-b border-border/20 flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-muted-foreground/70 shrink-0">
                        <Info size={12} /> Interaction Metadata
                    </div>
                    <div className="overflow-y-auto p-4 custom-scrollbar">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {metadataGroups.map((group: any, i: number) => (
                                <div key={i} className="space-y-2">
                                    <div className={cn("text-[10px] font-bold uppercase tracking-tight flex items-center gap-2", isPassed ? "text-emerald-600" : "text-rose-600")}>
                                        <div className="w-1 h-3 rounded-full bg-current opacity-50" />
                                        {group.name}
                                    </div>
                                    <div className="space-y-1 ml-3">
                                        {group.attributes.map((attr: any, j: number) => (
                                            Object.entries(attr).map(([k, v]: [string, any]) => (
                                                <div key={`${j}-${k}`} className="flex flex-col text-[11px] py-0.5 group">
                                                    <span className="text-muted-foreground/80 font-medium leading-tight">{k}</span>
                                                    <span className="text-foreground font-mono break-all leading-tight selection:bg-primary/20">{v}</span>
                                                </div>
                                            ))
                                        ))}
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {/* Payload */}
            {hasValidPayload && (
                <div className="flex-1 flex flex-col overflow-hidden bg-background">
                    <div className="bg-muted/40 px-3 py-1.5 border-b border-border/50 flex justify-between items-center shrink-0 gap-4">
                        <div className="flex items-center gap-2 shrink-0 text-muted-foreground">
                            <FileJson size={14} className="text-primary/60" />
                            <span className="text-[10px] font-mono uppercase tracking-widest">{payloads[0].name}</span>
                        </div>

                        {/* Search Toolbar */}
                        <div className="flex items-center bg-background border border-border rounded-md px-2 py-0.5 gap-2 grow max-w-md mx-auto group focus-within:ring-1 focus-within:ring-primary transition-all">
                            <Search size={12} className="text-muted-foreground group-focus-within:text-primary" />
                            <input
                                value={searchQuery}
                                onChange={(e) => { setSearchQuery(e.target.value); setCurrentIndex(0); }}
                                placeholder="Find..."
                                className="bg-transparent border-none outline-none text-[11px] text-foreground placeholder:text-muted-foreground w-full"
                                onKeyDown={(e) => {
                                    if (e.key === 'Enter') e.shiftKey ? prevMatch() : nextMatch();
                                    if (e.key === 'Escape' && searchQuery !== "") {
                                        setSearchQuery("");
                                        e.stopPropagation();
                                    }
                                }}
                            />
                            {searchQuery && (
                                <div className="flex items-center gap-1 shrink-0">
                                    <span className="text-[9px] font-mono text-muted-foreground px-1 border-r border-border mr-1">
                                        {matchCount > 0 ? `${currentIndex + 1}/${matchCount}` : "0/0"}
                                    </span>
                                    <button onClick={prevMatch} className="text-muted-foreground hover:text-foreground p-0.5"><ChevronUp size={12}/></button>
                                    <button onClick={nextMatch} className="text-muted-foreground hover:text-foreground p-0.5"><ChevronDown size={12}/></button>
                                    <button onClick={() => setSearchQuery("")} className="text-muted-foreground hover:text-destructive p-0.5 ml-1"><X size={12}/></button>
                                </div>
                            )}
                        </div>

                        <div className="flex items-center gap-1 shrink-0">
                            {onExpand ? (
                                <button onClick={onExpand} className="p-1.5 text-muted-foreground hover:text-primary transition-colors" title="Pop out"><ExternalLink size={14}/></button>
                            ) : onToggleMaximize ? (
                                <button
                                    onClick={onToggleMaximize}
                                    className={cn(
                                        "p-1.5 rounded-md transition-all flex items-center justify-center text-muted-foreground hover:text-primary"
                                    )}
                                    title={isMaximized ? "Exit Fullscreen" : "Maximize"}
                                >
                                    {isMaximized ? <X size={16}/> : <Maximize2 size={14}/>}
                                </button>
                            ) : null}
                            <button
                                onClick={() => setIsWrapped(!isWrapped)}
                                className={cn("p-1.5 rounded transition-colors", isWrapped ? "bg-primary text-primary-foreground" : "text-muted-foreground hover:text-foreground")}
                                title="Toggle Word Wrap"
                            >
                                <WrapText size={14}/>
                            </button>
                            <button onClick={onCopy} className="p-1.5 text-muted-foreground hover:text-foreground" title="Copy"><Copy size={14}/></button>
                            <span className="text-[9px] font-bold text-primary uppercase bg-primary/10 px-1.5 py-0.5 rounded border border-primary/20 ml-1">
                                {payloads[0].language}
                            </span>
                        </div>
                    </div>

                    <pre className={cn(
                        "flex-1 overflow-auto p-6 text-[13px] font-mono leading-relaxed selection:bg-primary/20 custom-scrollbar transition-all bg-transparent",
                        isWrapped ? "whitespace-pre-wrap" : "whitespace-pre"
                    )}>
                        <code ref={codeRef} className="hljs" dangerouslySetInnerHTML={{
                            __html: payloads[0].value ? hljs.highlight(payloads[0].value, {language: payloads[0].language?.toLowerCase() || 'json'}).value : ''
                        }}/>
                    </pre>
                </div>
            )}
        </div>
    );
};