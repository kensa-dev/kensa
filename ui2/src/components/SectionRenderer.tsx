import Sentence from './Sentence';
import { Tabs } from './Tabs';
import { FailureMessage } from './FailureMessage';
import { Section } from '@/constants';
import {useContext} from "react";
import {ConfigContext} from "@/contexts/ConfigContext.tsx";
import {Invocation} from "@/types/Test.ts";

interface SectionRendererProps {
    invocation: Invocation;
    testState: string;
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

                    case Section.Sentences:
                        return (
                            <div key={idx} className="space-y-1.5 my-4">
                                {invocation.sentences.map((line, sIdx: number) => (
                                    <Sentence key={sIdx} sentence={line} />
                                ))}
                            </div>
                        );

                    case Section.Exception:
                        return <div key={idx} className="my-4"><FailureMessage invocation={invocation} /></div>;

                    default:
                        return null;
                }
            })}
        </>
    );
};