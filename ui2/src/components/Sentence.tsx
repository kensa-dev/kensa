import * as React from 'react';
import {cn} from "@/lib/utils";
import Token from './Token';

interface SentenceProps {
    sentence: any[];
    isNested?: boolean;
}

const Sentence = ({ sentence, isNested = false }: SentenceProps) => {
    if (!sentence || sentence.length === 0) return null;

    return (
        <div className={cn(
            "sentence-container flex flex-wrap items-baseline leading-relaxed text-foreground",
            isNested ? "text-[12px]" : "text-[13px]"
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

                return (
                    <React.Fragment key={index}>
                        {index > 0 && !['tk-nl', 'tk-in', 'tk-bl'].some(t => sentence[index-1].types?.includes(t)) && (
                            <span className="select-none">&nbsp;</span>
                        )}
                        <Token token={token} />
                    </React.Fragment>
                );
            })}
        </div>
    );
};

export default Sentence;