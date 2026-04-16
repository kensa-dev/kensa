import Sentence from './Sentence';
import { Tabs } from './Tabs';
import { FailureMessage } from './FailureMessage';
import { Section } from '@/constants';
import {useContext, useState} from "react";
import {ConfigContext} from "@/contexts/ConfigContext";
import {FixtureHighlightProvider} from "@/contexts/FixtureHighlightContext";
import {Invocation, ParseError, RenderError, Sentence as SentenceType, TestState} from "@/types/Test";
import {isMajorKeyword} from "@/components/Token";
import {Separator} from "@/components/ui/separator";
import { getFailingLine } from '@/util/stackTrace';
import {Collapsible, CollapsibleContent, CollapsibleTrigger} from "@/components/ui/collapsible";
import {AlertTriangle, ChevronRight} from "lucide-react";
import {cn} from "@/lib/utils";

interface ProblemsPanelProps {
    parseErrors?: ParseError[];
    renderErrors?: RenderError[];
}

const ProblemsPanel = ({parseErrors, renderErrors}: ProblemsPanelProps) => {
    const hasParseErrors = (parseErrors?.length ?? 0) > 0;
    const hasRenderErrors = (renderErrors?.length ?? 0) > 0;
    if (!hasParseErrors && !hasRenderErrors) return null;

    const [open, setOpen] = useState(true);

    return (
        <Collapsible open={open} onOpenChange={setOpen} className="my-3 rounded-lg border border-amber-400/40 bg-amber-50/40 dark:bg-amber-950/20 overflow-hidden">
            <CollapsibleTrigger className="w-full cursor-pointer select-none px-3 py-2 text-[12px] font-semibold text-amber-700 dark:text-amber-400 flex items-center gap-1.5">
                <AlertTriangle className="h-3.5 w-3.5 shrink-0" />
                <span className="flex-1 text-left">Problems</span>
                <ChevronRight className={cn("h-3.5 w-3.5 shrink-0 transition-transform duration-200", open && "rotate-90")} />
            </CollapsibleTrigger>
            <CollapsibleContent>
                <div className="px-3 pb-3 space-y-1.5 pt-1">
                    {parseErrors && parseErrors.map((e, i) => (
                        <div key={i} className="text-[11px] font-mono text-amber-800 dark:text-amber-300">
                            <span className="opacity-60">line {e.line}:</span> {e.message}
                        </div>
                    ))}
                    {renderErrors && renderErrors.map((e, i) => (
                        <div key={i} className="text-[11px] font-mono text-red-700 dark:text-red-400">
                            <span className="opacity-60">[{e.type}]</span> {e.message}
                        </div>
                    ))}
                </div>
            </CollapsibleContent>
        </Collapsible>
    );
};

interface SectionRendererProps {
    invocation: Invocation;
    testState: TestState;
    autoOpenTab?: string;
    parseErrors?: ParseError[];
}

export const SectionRenderer = ({ invocation, testState, autoOpenTab, parseErrors }: SectionRendererProps) => {
    const {sectionOrder} = useContext(ConfigContext);

    return (
        <>
            <ProblemsPanel parseErrors={parseErrors} renderErrors={invocation.renderErrors} />
            {sectionOrder.map((section, idx) => {
                switch (section) {
                    case Section.Tabs:
                        return <div key={idx} className="mb-4"><Tabs invocation={invocation} testState={testState} autoOpenTab={autoOpenTab} /></div>;

                    case Section.Sentences: {
                        const sentences = invocation.sentences;

                        const isPureNote = (s: SentenceType) => {
                            const meaningful = s.tokens.filter(t => {
                                const types = t.types || [];
                                return !types.includes('tk-nl') && !types.includes('tk-in');
                            });
                            return meaningful.length > 0 && meaningful.every(t => (t.types || []).includes('tk-nt'));
                        };

                        // Separator goes before the earliest consecutive pure-note sentence(s)
                        // that immediately precede a new major keyword, so notes visually
                        // belong to the section they were written adjacent to.
                        const separatorAt = new Set<number>();
                        let prevMajorIdx = -1;
                        for (let i = 0; i < sentences.length; i++) {
                            const kw = sentences[i].tokens.find(t => t.types?.includes('tk-kw'))?.value ?? '';
                            if (isMajorKeyword(kw)) {
                                if (i > 0) {
                                    let sepIdx = i;
                                    for (let j = i - 1; j > prevMajorIdx; j--) {
                                        if (isPureNote(sentences[j])) sepIdx = j;
                                        else break;
                                    }
                                    separatorAt.add(sepIdx);
                                }
                                prevMajorIdx = i;
                            }
                        }

                        const sentenceLines = new Set(sentences.map(s => s.lineNumber));
                        const failingLine = testState === 'Failed' ? getFailingLine(invocation.executionException?.stackTrace ?? '', sentenceLines) : undefined;

                        let lastMajorKw = '';
                        return (
                            <FixtureHighlightProvider key={idx} fixtureSpecs={invocation.fixtureSpecs} fixtures={invocation.fixtures}>
                                <div className="my-4 space-y-0">
                                    {sentences.map(({ tokens, lineNumber }, sIdx) => {
                                        const kw = tokens.find(t => t.types?.includes('tk-kw'))?.value ?? '';
                                        const isMajor = isMajorKeyword(kw);
                                        if (isMajor) lastMajorKw = kw.toLowerCase().trim();
                                        const inherited = isMajor ? undefined : lastMajorKw;
                                        return (
                                            <div key={sIdx}>
                                                {separatorAt.has(sIdx) && <Separator className="my-2 opacity-30" />}
                                                <Sentence sentence={tokens} lineNumber={lineNumber} failingLine={failingLine} inheritedKeyword={inherited} />
                                            </div>
                                        );
                                    })}
                                </div>
                            </FixtureHighlightProvider>
                        );
                    }

                    case Section.Exception:
                        return <div key={idx} className="my-4"><FailureMessage invocation={invocation} /></div>;

                    default:
                        return null;
                }
            })}
        </>
    );
};