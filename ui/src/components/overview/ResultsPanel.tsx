import {Cell, Label, Pie, PieChart} from "recharts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {StateCounts} from "@/lib/overview";
import {cn} from "@/lib/utils";
import {PANEL_TITLE_CLASS, RESULT_CHART_CONFIG, RESULT_FILTER_TERMS, RESULT_KEYS, RESULT_TEXT_CLASSES, ResultKey} from "./chartConfig";

interface ResultsPanelProps {
    counts: StateCounts;
    onFilter: (term: string) => void;
}

interface Slice {
    key: ResultKey;
    value: number;
    fill: string;
}

interface SliceClickPayload {
    key?: ResultKey;
    payload?: {key?: ResultKey};
}

export function ResultsPanel({counts, onFilter}: ResultsPanelProps) {
    const present = RESULT_KEYS.filter(key => counts[key] > 0);
    const slices: Slice[] = present.map(key => ({key, value: counts[key], fill: `var(--color-${key})`}));
    const passRate = counts.total > 0 ? Math.round((counts.passed / counts.total) * 100) : 0;

    const onSliceClick = (slice: SliceClickPayload) => {
        const key = slice.payload?.key ?? slice.key;
        if (key) onFilter(RESULT_FILTER_TERMS[key]);
    };

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Results</CardTitle>
            </CardHeader>
            <CardContent className="flex items-center gap-3">
                <ChartContainer config={RESULT_CHART_CONFIG} className="aspect-square h-[130px] w-[130px] shrink-0">
                    <PieChart>
                        <ChartTooltip content={<ChartTooltipContent nameKey="key" hideLabel/>}/>
                        <Pie data={slices} dataKey="value" nameKey="key" innerRadius={45} outerRadius={60} paddingAngle={1} onClick={onSliceClick} isAnimationActive={false} className="cursor-pointer">
                            {slices.map(slice => <Cell key={slice.key} fill={slice.fill}/>)}
                            <Label content={({viewBox}) => viewBox && 'cx' in viewBox && 'cy' in viewBox
                                ? <text x={viewBox.cx} y={viewBox.cy} textAnchor="middle" dominantBaseline="middle" className="fill-foreground text-lg font-bold">{passRate}%</text>
                                : null}/>
                        </Pie>
                    </PieChart>
                </ChartContainer>
                <div className="flex flex-col gap-0.5">
                    {present.map(key => (
                        <button
                            key={key}
                            type="button"
                            onClick={() => onFilter(RESULT_FILTER_TERMS[key])}
                            className="-mx-1 flex items-baseline gap-1.5 rounded px-1 text-xs hover:bg-accent focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                        >
                            <span className={cn("font-mono font-bold tabular-nums", RESULT_TEXT_CLASSES[key])}>{counts[key]}</span>
                            <span className="text-muted-foreground">{RESULT_CHART_CONFIG[key].label}</span>
                        </button>
                    ))}
                    <span className="pt-1 text-[10px] text-muted-foreground">{counts.total} tests</span>
                </div>
            </CardContent>
        </Card>
    );
}
