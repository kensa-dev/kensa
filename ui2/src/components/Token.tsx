import * as React from 'react';
import {cn} from "@/lib/utils";
import {HoverCard, HoverCardContent, HoverCardTrigger,} from "@/components/ui/hover-card";
import {useConfig} from '@/contexts/ConfigContext';
import Sentence from "@/components/Sentence.tsx";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";

const Expandable = ({token}: any) => {
    const {value, parameterTokens, tokens} = token;
    const [isExpanded, setIsExpanded] = React.useState(false);
    const [leftOffset, setLeftOffset] = React.useState(0);
    const triggerRef = React.useRef<HTMLSpanElement>(null);

    React.useLayoutEffect(() => {
        if (isExpanded && triggerRef.current) {
            const triggerRect = triggerRef.current.getBoundingClientRect();
            const container = triggerRef.current.closest('.sentence-container');
            if (container) {
                const containerRect = container.getBoundingClientRect();
                setLeftOffset(triggerRect.left - containerRect.left);
            }
        }
    }, [isExpanded]);

    return (
        <HoverCard openDelay={200} closeDelay={100}>
            <HoverCardTrigger asChild>
                <span className="inline-flex items-baseline" ref={triggerRef}>
                    <span
                        onClick={() => setIsExpanded(!isExpanded)}
                        className={cn(
                            "cursor-pointer font-bold transition-colors select-none",
                            isExpanded
                                ? "text-primary underline decoration-primary/30"
                                : "text-blue-600 dark:text-blue-400 hover:underline"
                        )}
                    >
                        {value}
                    </span>
                    {parameterTokens?.length > 0 && (
                        <span className="ml-1.5 px-1 py-0.5 rounded bg-muted/50 text-[10px] font-mono border border-border/40 text-muted-foreground tracking-tighter">
                            {parameterTokens.map((t: any, i: number) => (
                                <Token key={i} token={t}/>
                            ))}
                        </span>
                    )}
                </span>
            </HoverCardTrigger>

            {!isExpanded && (
                <HoverCardContent
                    side="top"
                    align="start"
                    className={cn(
                        "p-4 shadow-2xl border-border/50 bg-background/95 backdrop-blur-md rounded-xl animate-in slide-in-from-bottom-1",
                        "w-[450px]"
                    )}
                >
                    <div className="space-y-1">
                        {tokens?.map((t: any, idx: number) => (
                            t.type === 'table'
                                ? <TokenTable key={idx} headers={t.headers} rows={t.rows}/>
                                : <Sentence key={idx} sentence={t} isNested={true}/>
                        ))}
                    </div>
                </HoverCardContent>
            )}

            {isExpanded && (
                <div
                    className="w-full mt-2 mb-2 pl-4 border-l-2 border-primary/20 bg-primary/[0.01] py-2 rounded-r-lg overflow-x-auto"
                    style={{marginLeft: `${leftOffset}px`}}
                >
                    {tokens?.map((t: any, idx: number) => (
                        t.type === 'table'
                            ? <TokenTable key={idx} headers={t.headers} rows={t.rows}/>
                            : <Sentence key={idx} sentence={t} isNested={true}/>
                    ))}
                </div>
            )}
        </HoverCard>
    );
};

const TokenTable = ({headers, rows}: { headers?: string[], rows: any[][] }) => {
    if (!rows) return null;
    return (<div className="my-3 rounded-md border border-border/50 bg-background/50 overflow-hidden shadow-sm">
        <Table>
            {headers && headers.length > 0 && (
                <TableHeader className="bg-muted/30">
                    <TableRow className="hover:bg-transparent">
                        {headers.map((header, i) => (
                            <TableHead key={i} className="h-8 text-[11px] font-bold uppercase tracking-wider text-muted-foreground py-2 px-4">
                                {header}
                            </TableHead>
                        ))}
                    </TableRow>
                </TableHeader>
            )}
            <TableBody>
                {rows.map((row, rowIdx) => (
                    <TableRow key={rowIdx} className="hover:bg-muted/20 transition-colors border-border/40">
                        {row.map((cell, cellIdx) => (
                            <TableCell key={cellIdx} className="py-2 px-4 text-xs font-medium">
                                <Token token={cell}/>
                            </TableCell>
                        ))}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    </div>)
};

const InfoToken = ({value, hint, acronymKey, tokenCls}: any) => {
    const {acronyms} = useConfig();
    const displayHint = hint || (acronymKey ? acronyms[acronymKey] : null);
    if (!displayHint) return <span className={tokenCls}>{value}</span>;

    return (
        <TooltipProvider>
            <Tooltip delayDuration={200}>
                <TooltipTrigger asChild>
                    <span className={cn("cursor-help border-b border-dotted border-muted-foreground/50 font-medium", tokenCls)}>
                        {value}
                    </span>
                </TooltipTrigger>
                <TooltipContent className="bg-slate-900 text-white border-none shadow-xl text-xs px-3 py-2 max-w-sm">
                    {displayHint}
                </TooltipContent>
            </Tooltip>
        </TooltipProvider>
    );
};
const NoteToken = ({value}: { value: string }) => {
    const {alwaysExpandNotes} = useConfig();
    const [isExpanded, setIsExpanded] = React.useState(alwaysExpandNotes);

    // Reset if global config changes
    React.useEffect(() => {
        setIsExpanded(alwaysExpandNotes);
    }, [alwaysExpandNotes]);

    return (
        <HoverCard openDelay={200} closeDelay={100}>
            <HoverCardTrigger asChild>
                <span
                    onClick={(e) => {
                        e.stopPropagation();
                        setIsExpanded(!isExpanded);
                    }}
                    className={cn(
                        "inline-flex items-baseline transition-all duration-200 ease-out group/note mx-1",
                        "border-l-[3px] border-amber-500/40 pl-1.5 pr-1 rounded-r-sm",
                        "leading-none align-baseline",

                        isExpanded ? "cursor-default border-l-[5px] ring-1 ring-amber-500/10" : "cursor-help",

                        isExpanded
                            ? "text-foreground bg-amber-500/[0.08] whitespace-pre-wrap"
                            : "text-amber-700/70 bg-amber-500/5 hover:bg-amber-500/10"
                    )}
                >
                    {isExpanded ? (
                        <span className="text-[13px] leading-normal">
                            {value}
                        </span>
                    ) : (
                        <span className="font-black tracking-widest text-[13px] opacity-60">
                            ···
                        </span>
                    )}
                </span>
            </HoverCardTrigger>

            {!isExpanded && (
                <HoverCardContent
                    side="top"
                    align="start"
                    className="w-80 p-0 shadow-2xl border-border/50 overflow-hidden rounded-xl"
                >
                    <div className="p-3 text-[13px] leading-relaxed text-foreground/90 bg-background whitespace-pre-wrap font-sans">
                        {value}
                    </div>
                </HoverCardContent>
            )}
        </HoverCard>
    );
};

const styles: Record<string, string> = {
    'tk-kw': 'text-blue-700 dark:text-blue-400 font-bold capitalize',
    'tk-wd': 'text-muted-foreground',
    'tk-id': 'text-foreground font-semibold',
    'tk-sl': 'text-amber-700 dark:text-amber-400 font-medium italic',
    'tk-fv': 'text-indigo-600 dark:text-indigo-400 font-mono text-[11px] bg-indigo-500/5 px-1 rounded border border-indigo-500/10',
    'tk-num': 'text-emerald-600 dark:text-emerald-400 font-mono font-bold',
    'tk-bo': 'text-orange-600 dark:text-orange-400 font-bold',
    'tk-null': 'text-rose-500 italic opacity-70',
    'tk-pph': 'text-foreground font-black italic tracking-tight'
};

const getMappedCls = (types: any[]) => types.map((t: string) => styles[t] || '').join(' ');

export const Token = ({token}: { token: any }) => {
    const {types = [], value, hint} = token;
    const tokenCls = types.join(" ");

    if (types.includes("tk-ex") || types.includes("tk-tab")) return <Expandable token={token}/>;
    if (types.includes("tk-ac")) return <InfoToken value={value} acronymKey={value} tokenCls={tokenCls}/>;
    if (types.includes("tk-hi")) return <InfoToken value={value} hint={hint} tokenCls={tokenCls}/>;
    if (types.includes("tk-nt")) return <NoteToken value={value}/>;
    if (types.includes("tk-tb")) return <div className="my-2 p-4 bg-muted/30 rounded-lg font-mono text-[11px] whitespace-pre-wrap leading-relaxed">{value}</div>;

    return <span className={cn(getMappedCls(types), "inline")}>{value}</span>;
};

export default Token;