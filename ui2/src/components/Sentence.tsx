import * as React from 'react';
import {cn} from "@/lib/utils";
import { Token as TokenType } from '@/types/Test';
import Token from "@/components/Token";

interface SentenceProps {
    sentence: TokenType[];
    lineNumber?: number;
    failingLine?: number;
    isNested?: boolean;
    inheritedKeyword?: string;
}

const Sentence = ({ sentence, lineNumber, failingLine, isNested = false, inheritedKeyword }: SentenceProps) => {
    if (!sentence || sentence.length === 0) return null;

    // Detect pure-note sentences (only tk-nt tokens, ignoring whitespace tokens)
    const meaningfulTokens = sentence.filter(t => {
        const types = t.types || [];
        return !types.includes('tk-nl') && !types.includes('tk-in');
    });
    const isPureNote = meaningfulTokens.length > 0 && meaningfulTokens.every(t => (t.types || []).includes('tk-nt'));
    if (isPureNote) {
        return (
            <div className="my-1 border-l-2 border-muted-foreground/20 pl-3 py-0.5 bg-muted/20 rounded-r-sm">
                {meaningfulTokens.map((token, i) => (
                    <p key={i} className="text-[13px] italic text-foreground/60 leading-relaxed whitespace-pre-wrap">{token.value}</p>
                ))}
            </div>
        );
    }

    // Split tokens into lines at tk-nl boundaries
    const lines: TokenType[][] = [[]];
    for (const token of sentence) {
        if (token.types?.includes('tk-nl')) {
            lines.push([]);
        } else {
            lines[lines.length - 1].push(token);
        }
    }

    // firstKwSeen tracks across all lines so only the first keyword gets inherited context
    let firstKwSeen = false;
    const isFailingLine = failingLine !== undefined && lineNumber === failingLine;

    return (
        <div className={cn(
            "sentence-container text-[15px] leading-relaxed text-foreground",
            isNested && "ml-1 border-muted pl-3 py-0.5 my-1",
            isFailingLine && "border-l-[3px] border-red-500/90 bg-gradient-to-r from-red-500/[0.08] dark:from-red-500/[0.13] to-transparent pl-3 py-0.5 rounded-r-sm"
        )}>
            {lines.map((line, lineIdx) => (
                <div key={lineIdx} className="flex flex-wrap items-baseline">
                    {line.map((token, tokenIdx) => {
                        const types = token.types || [];

                        if (types.includes('tk-in')) {
                            return <div key={tokenIdx} className="w-4 shrink-0" />;
                        }

                        const isFirstKw = types.includes('tk-kw') && !firstKwSeen;
                        if (isFirstKw) firstKwSeen = true;

                        return (
                            <React.Fragment key={tokenIdx}>
                                {tokenIdx > 0 && !line[tokenIdx - 1].types?.includes('tk-in') && (
                                    <span className="select-none">&nbsp;</span>
                                )}
                                <Token token={token} inheritedKeyword={isFirstKw ? inheritedKeyword : undefined} />
                            </React.Fragment>
                        );
                    })}
                </div>
            ))}
        </div>
    );
};

export default Sentence;