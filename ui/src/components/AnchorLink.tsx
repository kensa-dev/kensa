import React, {useState} from 'react';
import {Check, Link as LinkIcon} from 'lucide-react';
import {anchorHash, copyLink} from '@/util/anchorLink';
import {cn} from '@/lib/utils';

interface AnchorLinkProps {
    testId: string;
    method?: string;
    invocation?: number;
    className?: string;
}

export const AnchorLink = ({testId, method, invocation, className}: AnchorLinkProps) => {
    const [copied, setCopied] = useState(false);

    const onClick = async (e: React.MouseEvent) => {
        e.stopPropagation();
        const url = `${window.location.origin}${window.location.pathname}${window.location.search}${anchorHash(testId, method, invocation)}`;
        if (await copyLink(url)) {
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        }
    };

    return (
        <button
            onClick={onClick}
            title="Copy link"
            aria-label="Copy link"
            className={cn(
                'opacity-0 group-hover/anchor:opacity-100 focus-visible:opacity-100 transition-opacity',
                'p-0.5 rounded text-muted-foreground hover:text-foreground shrink-0',
                className,
            )}
        >
            {copied ? <Check size={13} className="text-success"/> : <LinkIcon size={13}/>}
        </button>
    );
};
