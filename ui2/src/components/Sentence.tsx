import * as React from 'react';
import {cn} from "@/lib/utils";
import { Token as TokenType } from '@/types/Test';
import Token from "@/components/Token";

interface SentenceProps {
    sentence: TokenType[];
    isNested?: boolean;
    inheritedKeyword?: string;
}

const Sentence = ({ sentence, isNested = false, inheritedKeyword }: SentenceProps) => {
    if (!sentence || sentence.length === 0) return null;

    // Detect pure-note sentences (only tk-nt tokens, ignoring whitespace tokens)
    const meaningfulTokens = sentence.filter(t => {
        const types = t.types || [];
        return !types.includes('tk-nl') && !types.includes('tk-in') && !types.includes('tk-bl');
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

    // Only the first tk-kw token in a sentence gets the inherited context
    let firstKwSeen = false;

    return (
        <div className={cn(
            "sentence-container flex flex-wrap items-baseline leading-relaxed text-foreground",
            "text-[15px] leading-relaxed",
            isNested && "ml-1 border-muted pl-3 py-0.5 my-1"
        )}>
            {sentence.map((token, index) => {
                const types = token.types || [];

                if (types.includes('tk-nl')) {
                    return <div key={index} className="w-full h-0" />;
                }
                if (types.includes('tk-in')) {
                    return <div key={index} className="w-6 shrink-0" />;
                }
                if (types.includes('tk-bl')) {
                    return <div key={index} className="w-full h-3 shrink-0" />;
                }

                const isFirstKw = types.includes('tk-kw') && !firstKwSeen;
                if (isFirstKw) firstKwSeen = true;

                return (
                    <React.Fragment key={index}>
                        {index > 0 && !['tk-nl', 'tk-in', 'tk-bl'].some(t => sentence[index-1].types?.includes(t)) && (
                            <span className="select-none">&nbsp;</span>
                        )}
                        <Token token={token} inheritedKeyword={isFirstKw ? inheritedKeyword : undefined} />
                    </React.Fragment>
                );
            })}
        </div>
    );
};

export default Sentence;