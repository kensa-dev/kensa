import * as React from 'react';
import {cn} from "@/lib/utils";
import {HoverCard, HoverCardContent, HoverCardTrigger,} from "@/components/ui/hover-card";
import {useConfig} from '@/contexts/ConfigContext';
import Sentence from "@/components/Sentence.tsx";
import {Tooltip, TooltipContent, TooltipProvider, TooltipTrigger} from "@/components/ui/tooltip.tsx";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table";
import {NestedItem, Token as TokenType} from '@/types/Test';

const Expandable = ({token}: { token: TokenType }) => {
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
                            "cursor-pointer font-black transition-all select-none border-b-2",
                            isExpanded
                                ? "text-primary border-primary/40 bg-primary/5 px-1 rounded-t-sm"
                                : "text-foreground/80 border-border hover:border-primary/50 hover:text-foreground"
                        )}
                    >
                            {value}
                        </span>
                    {parameterTokens && parameterTokens?.length > 0 && (
                        <span className="ml-1.5 px-1.5 py-0.5 rounded bg-muted/50 text-[11px] font-mono border border-border/40 text-muted-foreground tracking-tighter">
                            {parameterTokens.map((t, i) => (
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
                        {tokens?.map((item: NestedItem, idx: number) => {
                            if (!Array.isArray(item) && item.type === 'table') {
                                return <TokenTable key={idx} headers={item.headers} rows={item.rows}/>;
                            }

                            return <Sentence key={idx} sentence={item as TokenType[]} isNested={true}/>;
                        })}
                    </div>
                </HoverCardContent>
            )}

            {isExpanded && (
                <div
                    className="w-full mt-2 mb-2 pl-4 border-l-2 border-primary/20 bg-primary/[0.01] py-2 rounded-r-lg overflow-x-auto"
                    style={{marginLeft: `${leftOffset}px`}}
                >
                    {tokens?.map((item: NestedItem, idx: number) => {
                        if (!Array.isArray(item) && item.type === 'table') {
                            return <TokenTable key={idx} headers={item.headers} rows={item.rows}/>;
                        }

                        return <Sentence key={idx} sentence={item as TokenType[]} isNested={true}/>;
                    })}
                </div>
            )}
        </HoverCard>
    );
};

const TokenTable = ({headers, rows}: { headers?: string[], rows: TokenType[][] }) => {
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

const InfoToken = ({value, tooltipContent, tokenCls}: { value: string, tooltipContent: string | null, tokenCls: string }) => {
    if (!tooltipContent) return <span className={tokenCls}>{value}</span>;

    return (
        <TooltipProvider>
            <Tooltip delayDuration={200}>
                <TooltipTrigger asChild>
                    <span className={cn("cursor-help border-b border-dotted border-muted-foreground/50 font-medium", tokenCls)}>
                        {value}
                    </span>
                </TooltipTrigger>
                <TooltipContent className="bg-slate-900 text-white border-none shadow-xl text-xs px-3 py-2 max-w-sm">
                    {tooltipContent}
                </TooltipContent>
            </Tooltip>
        </TooltipProvider>
    );
};

const NoteToken = ({value}: { value: string }) => {
    const {alwaysExpandNotes} = useConfig();
    const [isExpanded, setIsExpanded] = React.useState(alwaysExpandNotes);

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
                        "border-l-2 border-muted-foreground/30 pl-1.5 pr-1 rounded-r-sm",
                        "leading-none align-baseline",

                        isExpanded ? "cursor-default border-l-4 bg-muted/40" : "cursor-help bg-muted/20 hover:bg-muted/60",

                        isExpanded
                            ? "text-foreground whitespace-pre-wrap"
                            : "text-muted-foreground/60"
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
    'tk-kw': 'text-foreground/90 font-bold tracking-tight opacity-80',
    'tk-wd': 'text-muted-foreground/70 font-normal',
    'tk-id': 'text-foreground font-semibold underline decoration-border/40 underline-offset-4',
    'tk-sl': 'text-foreground/90 font-medium italic',
    'tk-fv': 'font-mono font-bold text-foreground bg-muted/40 px-1.5 py-0.5 rounded border border-border/10',
    'tk-pv': 'font-mono font-bold text-foreground bg-muted/40 px-1.5 py-0.5 rounded border border-border/10',
    'tk-num': 'font-mono font-bold text-foreground/90',
    'tk-bo': 'font-mono font-semibold text-foreground/60',
    'tk-null': 'text-muted-foreground/30 italic line-through decoration-muted-foreground/10',
    'tk-pph': 'text-foreground font-semibold underline decoration-foreground/30 underline-offset-[6px] tracking-tight'
};
const getMappedCls = (types: string[]) => types.map((t: string) => styles[t] || '').join(' ');

export const Token = ({token}: { token: TokenType }) => {
    const {acronyms} = useConfig();
    const {types = [], value, hint} = token;
    const tokenCls = types.join(" ");

    if (types.includes("tk-ex") || types.includes("tk-tab")) return <Expandable token={token}/>;
    if (types.includes("tk-ac")) return <InfoToken value={value} tooltipContent={value ? acronyms[value] : null} tokenCls={tokenCls}/>;
    if (types.includes("tk-hi")) return <InfoToken value={value} tooltipContent={hint ? hint : null} tokenCls={tokenCls}/>;
    if (types.includes("tk-nt")) return <NoteToken value={value}/>;
    if (types.includes("tk-tb")) return <div className="my-2 p-4 bg-muted/30 rounded-lg font-mono text-[11px] whitespace-pre-wrap leading-relaxed">{value}</div>;

    return <span className={cn(getMappedCls(types), "inline")}>{value}</span>;
};

export default Token;