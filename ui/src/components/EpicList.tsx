import {IssueBadge} from './IssueBadge';
import {TestState} from "@/types/Test";

interface EpicListProps {
    epics?: string[];
    testState: TestState;
}

export const EpicList = ({epics = [], testState}: EpicListProps) => {
    if (epics.length === 0) return null;
    return (
        <div className="flex items-center gap-1 shrink-0">
            <span className="text-[9px] font-black uppercase tracking-widest text-muted-foreground/60">Epic</span>
            {epics.map(epic => <IssueBadge key={epic} issue={epic} testState={testState}/>)}
        </div>
    );
};
