import React, {useState} from 'react';
import {Check, Code, Link as LinkIcon} from 'lucide-react';
import {anchorHash, copyLink, embedHash} from '@/util/anchorLink';
import {reportBase} from '@/util/linkBase';
import {useConfig} from '@/contexts/ConfigContext';
import {cn} from '@/lib/utils';

interface AnchorLinkProps {
    testId: string;
    method?: string;
    invocation?: number;
    embed?: boolean;
    className?: string;
}

// `embed` copies the chromeless `#/embed/` link for pasting into a host page.
export const AnchorLink = ({testId, method, invocation, embed = false, className}: AnchorLinkProps) => {
    const [copied, setCopied] = useState(false);
    const config = useConfig();

    const onClick = async (e: React.MouseEvent) => {
        e.stopPropagation();
        const hash = embed ? embedHash(testId, method, invocation) : anchorHash(testId, method, invocation);
        const url = `${reportBase(config, window.location)}${hash}`;
        if (await copyLink(url)) {
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        }
    };

    return (
        <button
            onClick={onClick}
            title={embed ? 'Copy embed link' : 'Copy link'}
            aria-label={embed ? 'Copy embed link' : 'Copy link'}
            className={cn(
                'opacity-0 group-hover/anchor:opacity-100 focus-visible:opacity-100 transition-opacity',
                'p-0.5 rounded text-muted-foreground hover:text-foreground shrink-0',
                className,
            )}
        >
            {copied ? <Check size={13} className="text-success"/> : embed ? <Code size={13}/> : <LinkIcon size={13}/>}
        </button>
    );
};
