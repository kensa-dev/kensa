import * as React from 'react';
import { cn } from '@/lib/utils';

// ---------------------------------------------------------------------------
// Inline markdown parser — supports **bold**, *italic*, __underline__,
// ~~strikethrough~~, and [label](url) links.
// Blank lines become paragraph breaks; single newlines become <br>.
// ---------------------------------------------------------------------------

type InlineNode =
    | { type: 'text'; value: string }
    | { type: 'bold'; children: InlineNode[] }
    | { type: 'italic'; children: InlineNode[] }
    | { type: 'underline'; children: InlineNode[] }
    | { type: 'strike'; children: InlineNode[] }
    | { type: 'link'; href: string; children: InlineNode[] };

// Group map:
//  m[1]/m[2]   **bold**
//  m[3]/m[4]   __underline__  (single-line only — avoids conflicts with _italic_)
//  m[5]/m[6]   ~~strike~~
//  m[7]/m[8]   *italic*
//  m[9]/m[10]/m[11]  [text](href)
// NOTE: regex is created fresh per call — a module-level /g regex has shared
// mutable lastIndex which causes infinite loops when parseInline recurses.
const INLINE_PATTERN =
    /(\*\*([\s\S]+?)\*\*)|(__([^\n]+?)__)|(~~([\s\S]+?)~~)|(\*([\s\S]+?)\*)|(\[([^\]]+)\]\(([^)]+)\))/g;

function parseInline(text: string): InlineNode[] {
    const nodes: InlineNode[] = [];
    let last = 0;
    let m: RegExpExecArray | null;

    const re = new RegExp(INLINE_PATTERN.source, 'g');

    while ((m = re.exec(text)) !== null) {
        if (m.index > last) {
            nodes.push({ type: 'text', value: text.slice(last, m.index) });
        }

        if (m[1] !== undefined) {
            nodes.push({ type: 'bold', children: parseInline(m[2]) });
        } else if (m[3] !== undefined) {
            nodes.push({ type: 'underline', children: parseInline(m[4]) });
        } else if (m[5] !== undefined) {
            nodes.push({ type: 'strike', children: parseInline(m[6]) });
        } else if (m[7] !== undefined) {
            nodes.push({ type: 'italic', children: parseInline(m[8]) });
        } else if (m[9] !== undefined) {
            nodes.push({ type: 'link', href: m[11], children: parseInline(m[10]) });
        }

        last = m.index + m[0].length;
    }

    if (last < text.length) {
        nodes.push({ type: 'text', value: text.slice(last) });
    }

    return nodes;
}

type OnTestLink = ((method: string) => void) | undefined;

function renderInlineNodes(nodes: InlineNode[], keyPrefix = '', onTestLink?: OnTestLink): React.ReactNode[] {
    return nodes.flatMap((node, i) => {
        const key = `${keyPrefix}${i}`;
        switch (node.type) {
            case 'text': {
                // Render single newlines as <br>
                const parts = node.value.split('\n');
                return parts.flatMap((part, pi) =>
                    pi < parts.length - 1
                        ? [part, <br key={`${key}-br${pi}`} />]
                        : [part]
                );
            }
            case 'bold':
                return [<strong key={key} className="font-semibold">{renderInlineNodes(node.children, key, onTestLink)}</strong>];
            case 'italic':
                return [<em key={key}>{renderInlineNodes(node.children, key, onTestLink)}</em>];
            case 'underline':
                return [<u key={key} className="underline underline-offset-2">{renderInlineNodes(node.children, key, onTestLink)}</u>];
            case 'strike':
                return [<s key={key} className="line-through opacity-60">{renderInlineNodes(node.children, key, onTestLink)}</s>];
            case 'link':
                if (node.href.startsWith('#')) {
                    const method = node.href.slice(1);
                    return [
                        <button
                            key={key}
                            type="button"
                            onClick={() => onTestLink?.(method)}
                            className={cn(
                                "underline underline-offset-2 decoration-amber-400/60 cursor-pointer",
                                "text-amber-600 dark:text-amber-400",
                                "hover:text-amber-800 dark:hover:text-amber-300 transition-colors"
                            )}
                        >
                            {renderInlineNodes(node.children, key, onTestLink)}
                        </button>
                    ];
                }
                return [
                    <a
                        key={key}
                        href={node.href}
                        target="_blank"
                        rel="noreferrer"
                        className={cn(
                            "underline underline-offset-2 decoration-amber-400/60",
                            "text-amber-600 dark:text-amber-400",
                            "hover:text-amber-800 dark:hover:text-amber-300 transition-colors"
                        )}
                    >
                        {renderInlineNodes(node.children, key, onTestLink)}
                    </a>
                ];
        }
    });
}

// Detect markdown table: every non-empty line starts with '|'
function isTable(paragraph: string): boolean {
    const lines = paragraph.trim().split('\n').filter(l => l.trim().length > 0);
    return lines.length >= 2 && lines.every(l => l.trim().startsWith('|'));
}

function renderTable(paragraph: string, blockIndex: number, onTestLink?: OnTestLink): React.ReactNode {
    const lines = paragraph.trim().split('\n').filter(l => l.trim().length > 0);
    const parseRow = (line: string) =>
        line.trim().replace(/^\||\|$/g, '').split('|').map(cell => cell.trim());

    const isSeparator = (line: string) => /^[\s|:\-]+$/.test(line);

    const headers = parseRow(lines[0]);
    const dataLines = lines.slice(1).filter(l => !isSeparator(l));

    return (
        <div key={blockIndex} className={cn("overflow-x-auto", blockIndex > 0 && "mt-2")}>
            <table className="text-sm w-full border-collapse">
                <thead>
                    <tr>
                        {headers.map((h, i) => (
                            <th key={i} className="text-left px-3 py-1.5 font-semibold text-foreground/70 border-b border-amber-300/40 whitespace-nowrap">
                                {renderInlineNodes(parseInline(h), `th-${blockIndex}-${i}`, onTestLink)}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {dataLines.map((line, ri) => (
                        <tr key={ri} className="even:bg-amber-100/30 dark:even:bg-amber-900/10">
                            {parseRow(line).map((cell, ci) => (
                                <td key={ci} className="px-3 py-1 text-foreground/80 border-b border-amber-200/30 dark:border-amber-800/20">
                                    {renderInlineNodes(parseInline(cell), `td-${blockIndex}-${ri}-${ci}`, onTestLink)}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

// Split on blank lines → paragraphs; render each paragraph's inline markdown.
function renderParagraphs(text: string, onTestLink?: OnTestLink): React.ReactNode {
    return text
        .split(/\n\n+/)
        .filter(p => p.trim().length > 0)
        .map((paragraph, i) =>
            isTable(paragraph)
                ? renderTable(paragraph, i, onTestLink)
                : (
                    <p key={i} className={cn("text-sm leading-relaxed text-foreground/80", i > 0 && "mt-2")}>
                        {renderInlineNodes(parseInline(paragraph.trim()), '', onTestLink)}
                    </p>
                )
        );
}

// ---------------------------------------------------------------------------
// Component
// ---------------------------------------------------------------------------

interface NotesCardProps {
    notes: string;
    onTestLink?: (method: string) => void;
    className?: string;
}

export const NotesCard = ({ notes, onTestLink, className }: NotesCardProps) => {
    const content = React.useMemo(() => renderParagraphs(notes, onTestLink), [notes, onTestLink]);

    return (
        <div
            className={cn(
                // shape — mirrors collapsed TestCard
                "rounded-xl shadow-sm overflow-hidden",
                // amber border with left accent
                "border border-amber-300/40 dark:border-amber-700/30",
                "border-l-[3px] border-l-amber-400/70 dark:border-l-amber-500/60",
                // fill
                "bg-amber-50/60 dark:bg-amber-950/25",
                // layout
                "px-5 py-3",
                className
            )}
        >
            <p className="text-[10px] font-black uppercase tracking-widest text-amber-500/55 dark:text-amber-400/50 mb-1.5 select-none">
                Notes
            </p>
            {content}
        </div>
    );
};
