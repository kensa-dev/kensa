import {ComponentDiagram} from "@/components/ComponentDiagram";

interface SystemViewPageProps {
    aggregateComponentDiagram: string | undefined | null;
}

export function SystemViewPage({aggregateComponentDiagram}: SystemViewPageProps) {
    return (
        <div className="p-6 lg:p-4">
            <div className="max-w-[1400px] mx-auto space-y-4">
                <div>
                    <h1 className="text-xl font-black text-foreground">System View</h1>
                    <p className="text-sm text-muted-foreground mt-1">Aggregate of interactions captured across all tests.</p>
                </div>

                {aggregateComponentDiagram ? (
                    <ComponentDiagram svg={aggregateComponentDiagram}/>
                ) : (
                    <p className="text-sm text-muted-foreground italic">No system-level interactions captured.</p>
                )}
            </div>
        </div>
    );
}
