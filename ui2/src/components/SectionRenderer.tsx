import Sentence from './Sentence';
import { Tabs } from './Tabs';
import { FailureMessage } from './FailureMessage';
import { Section } from '@/constants';
import {useContext} from "react";
import {ConfigContext} from "@/contexts/ConfigContext";
import {FixtureHighlightProvider} from "@/contexts/FixtureHighlightContext";
import {Invocation, Sentence as SentenceType, TestState} from "@/types/Test";
import {isMajorKeyword} from "@/components/Token";
import {Separator} from "@/components/ui/separator";

const STDLIB_SKIP = /\s+at (java\.|kotlin\.|org\.junit\.|dev\.kensa\.(parse|state|context|output|sentence|render|junit|kotest|testng))/;

function getFailingLine(stackTrace: string, sentenceLines: Set<number>): number | undefined {
    for (const line of stackTrace.split('\n')) {
        if (!line.includes('\tat ') || STDLIB_SKIP.test(line)) continue;
        const m = line.match(/:(\d+)\)$/);
        if (m) {
            const n = parseInt(m[1]);
            if (sentenceLines.has(n)) return n;
        }
    }
    return undefined;
}

interface SectionRendererProps {
    invocation: Invocation;
    testState: TestState;
    autoOpenTab?: string;
}

export const SectionRenderer = ({ invocation, testState, autoOpenTab }: SectionRendererProps) => {
    const {sectionOrder} = useContext(ConfigContext);

    return (
        <>
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
                                if (prevMajorIdx >= 0) {
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