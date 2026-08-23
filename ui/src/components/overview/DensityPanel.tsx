import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {DensityModel} from "@/lib/overview";
import {PANEL_TITLE_CLASS, simpleName} from "./chartConfig";
import {StatTile} from "./StatTile";

interface DensityPanelProps {
    model: DensityModel;
    onNavigate: (classId: string, testMethod: string) => void;
}

export function DensityPanel({model, onNavigate}: DensityPanelProps) {
    const {weakest} = model;

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Specification density</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex flex-wrap gap-6">
                    <StatTile label="assertions / test" value={model.assertionsPerTest.toFixed(1)}/>
                    <StatTile label="tests using expandables" value={`${Math.round(model.expandableShare * 100)}%`}/>
                    <StatTile label="parameterised tests" value={`${model.parameterised}`}/>
                </div>
                {weakest && (
                    <p className="pt-3 text-[10px] text-muted-foreground">
                        Weakest:{' '}
                        <button
                            type="button"
                            onClick={() => onNavigate(weakest.classId, '')}
                            title={weakest.testClass}
                            className="rounded font-mono hover:underline focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                        >
                            {simpleName(weakest.testClass)}
                        </button>
                        {' '}({weakest.assertionsPerTest.toFixed(1)} assertions / test)
                    </p>
                )}
            </CardContent>
        </Card>
    );
}
