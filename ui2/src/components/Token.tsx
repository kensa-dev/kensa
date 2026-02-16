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

    const isMultilineParams = Boolean(parameterTokens?.[0]?.types?.includes("tk-nl"));

    const splitParameterLines = (pts: TokenType[]) => {
        const lines: TokenType[][] = [];
        let current: TokenType[] = [];

        for (const t of pts) {
            const types = t.types || [];
            if (types.includes("tk-nl")) {
                lines.push(current);
                current = [];
                continue;
            }
            current.push(t);
        }
        if (current.length > 0) lines.push(current);

        return lines;
    };

    const renderParameterLine = (line: TokenType[], lineIdx: number) => {
        let i = 0;
        while (i < line.length && (line[i].types || []).includes("tk-in")) i += 1;

        const indentCount = i;
        const contentTokens = line.slice(i);

        return (
            <div key={`param-line-${lineIdx}`} className="flex items-baseline">
                {indentCount > 0 && (
                    <span
                        aria-hidden="true"
                        className="shrink-0"
                        style={{ width: `${indentCount * 1.5}rem` }}
                    />
                )}

                <span className="flex flex-wrap items-baseline gap-x-1 gap-y-0.5">
                    {contentTokens.map((t, idx) => (
                        <Token key={`param-${lineIdx}-${idx}`} token={t} />
                    ))}
                </span>
            </div>
        );
    };

    const renderParameterTokens = (pts: TokenType[]) => {
        if (!isMultilineParams) {
            return (
                <span className="inline-flex flex-wrap items-baseline gap-x-1 whitespace-pre-wrap break-words">
                    {pts.map((t, idx) => (
                        <Token key={`param-inline-${idx}`} token={t}/>
                    ))}
                </span>
            );
        }

        const lines = splitParameterLines(pts);

        const normalized = lines.length > 0 && lines[0].length === 0 ? lines.slice(1) : lines;

        return (
            <div className="space-y-0.5">
                {normalized.map(renderParameterLine)}
            </div>
        );
    };

    return (
        <HoverCard openDelay={200} closeDelay={100}>
            <HoverCardTrigger asChild>
                <span className={cn("inline-flex items-baseline", "flex-wrap")} ref={triggerRef}>
                    <span
                        onClick={() => setIsExpanded(!isExpanded)}
                        className={cn(
                            "cursor-pointer font-black transition-all select-none",
                            "underline underline-offset-2 decoration-2",
                            isExpanded
                                ? "text-primary decoration-primary/60 bg-primary/5 px-1 rounded-t-sm"
                                : "text-foreground/80 decoration-border hover:decoration-primary/60 hover:text-foreground"
                        )}
                    >
                        {value}
                    </span>

                    {parameterTokens && parameterTokens.length > 0 && (
                        <div
                            className={cn(
                                "py-0.5 rounded bg-muted/40 border border-border/20 text-foreground/90",
                                "leading-snug",
                                isMultilineParams ? "basis-full mt-1 ml-0 max-w[56-rem] w-fit" : "ml-1.5"
                            )}
                        >
                            {renderParameterTokens(parameterTokens)}
                        </div>
                    )}
                </span>
            </HoverCardTrigger>

            {!isExpanded && (
                <HoverCardContent
                    side="top"
                    align="start"
                    className={"p-4 shadow-2xl border-border/50 bg-background/95 backdrop-blur-md rounded-xl animate-in slide-in-from-bottom-1 w-[450px]"}
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
                    className="w-full mt-2 mb-2 pl-4 border-l-2 border-primary/20 bg-primary/[0.01] py-0.5 rounded-r-lg overflow-x-auto"
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
                            <TableCell key={cellIdx} className="py-2 px-4 text-xs">
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
                    <span
                        className={cn(
                            "cursor-help font-medium",
                            "underline decoration-dotted decoration-muted-foreground/60 underline-offset-2",
                            "hover:decoration-muted-foreground",
                            tokenCls
                        )}
                    >
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
                        <span className="leading-normal">
                            {value}
                        </span>
                    ) : (
                        <span className="font-black tracking-widest opacity-60">
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
                    <div className="p-3 leading-relaxed text-foreground/90 bg-background whitespace-pre-wrap font-sans">
                        {value}
                    </div>
                </HoverCardContent>
            )}
        </HoverCard>
    );
};

const styles: Record<string, string> = {
    'tk-kw': 'text-foreground font-bold tracking-tight',
    'tk-wd': 'text-foreground/90',
    'tk-id': 'text-foreground',
    'tk-sl': 'text-foreground/90 italic',
    'tk-fv': 'text-foreground bg-muted/20 px-0.5 py-0.5 rounded border border-border/15',
    'tk-pv': 'text-foreground bg-muted/20 px-0.5 py-0.5 rounded border border-border/15',
    'tk-num': 'font-semibold text-foreground',
    'tk-bo': 'font-semibold text-foreground/80',
    'tk-null': 'text-muted-foreground/70 italic line-through decoration-muted-foreground/30',
    'tk-pph': 'text-foreground tracking-tight'
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
    if (types.includes("tk-tb")) return <div className="my-2 p-4 bg-muted/30 rounded-lg text-[11px] whitespace-pre-wrap leading-relaxed">{value}</div>;

    return <span className={cn(getMappedCls(types), "inline")}>{value}</span>;
};

export default Token;