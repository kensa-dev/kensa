import {ExternalLink} from 'lucide-react';
import KensaLogo from '@/assets/logo.svg?react';
import {cn} from '@/lib/utils';
import {EmbedParams, EmbedTarget} from '@/util/embedRoute';
import {SelectedIndex} from '@/types/Index';
import {TestDetail, TestState} from '@/types/Test';
import {NotesCard} from './NotesCard';
import {TestContainer} from './TestContainer';
import {useReportHeight} from '@/hooks/useReportHeight';

const stateDot: Record<TestState, string> = {
    Passed: 'bg-success',
    Failed: 'bg-failure',
    Disabled: 'bg-disabled',
    'Not Executed': 'bg-muted-foreground',
};

interface EmbedPageProps {
    testId: string;
    params: EmbedParams;
    target: EmbedTarget | null;
    selectedIndex: SelectedIndex | null;
    testDetail: TestDetail | null;
    isLoading: boolean;
    testToExpand: string;
    invocationToExpand: number;
    onTestLink: (method: string) => void;
    reportUrl: string;
}

// The chromeless view a host page frames: the test container and one slim
// footer that names the class and links out to the full report.
export const EmbedPage = ({testId, params, target, selectedIndex, testDetail, isLoading, testToExpand, invocationToExpand, onTestLink, reportUrl}: EmbedPageProps) => {
    useReportHeight();

    return (
        <div className="bg-background font-sans p-2">
            {target && !target.found ? (
                <div className="bg-card border rounded-xl shadow-sm px-5 py-3 text-sm text-muted-foreground">
                    {target.missing === 'test'
                        ? <>No test <span className="font-mono text-foreground">{testId}</span> in this report.</>
                        : <>No method <span className="font-mono text-foreground">{params.method}</span> in <span className="font-mono text-foreground">{selectedIndex?.testClass ?? testId}</span>.</>}
                </div>
            ) : !target || isLoading || !testDetail || !selectedIndex ? (
                <div className="bg-card border rounded-xl shadow-sm px-5 py-3 flex items-center gap-2" aria-busy="true">
                    <span className="h-4 w-4 rounded-full bg-muted animate-pulse"/>
                    <span className="h-3.5 w-56 max-w-full rounded bg-muted animate-pulse"/>
                </div>
            ) : (
                <div className="space-y-4">
                    {params.notes && testDetail.notes && (
                        <NotesCard notes={testDetail.notes} onTestLink={onTestLink}/>
                    )}
                    <TestContainer
                        key={`${testId}-${testToExpand}`}
                        tests={testDetail.tests}
                        testClass={testDetail.testClass}
                        testId={testId}
                        testToExpand={testToExpand}
                        invocationToExpand={invocationToExpand}
                    />
                </div>
            )}
            <footer className="mt-2 px-1 flex items-center gap-2 text-[11px] text-muted-foreground print:hidden">
                <KensaLogo className="w-4 h-4 text-success shrink-0"/>
                {selectedIndex && (
                    <>
                        <span className="font-mono truncate">{selectedIndex.testClass}</span>
                        <span className={cn('w-1.5 h-1.5 rounded-full shrink-0', stateDot[selectedIndex.state])}/>
                        <span>{selectedIndex.state}</span>
                    </>
                )}
                <a
                    href={reportUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="ml-auto inline-flex items-center gap-1 hover:text-foreground transition-colors whitespace-nowrap"
                >
                    Open full report
                    <ExternalLink size={11}/>
                </a>
            </footer>
        </div>
    );
};
