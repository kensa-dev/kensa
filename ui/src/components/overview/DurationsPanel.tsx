import {Bar, BarChart, XAxis, YAxis} from "recharts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {DurationBucket} from "@/lib/overview";
import {PANEL_TITLE_CLASS, SERIES_COLOURS} from "./chartConfig";

const CHART_CONFIG = {
    count: {label: 'Tests', color: SERIES_COLOURS[0]},
} satisfies ChartConfig;

interface DurationsPanelProps {
    buckets: DurationBucket[];
    onBucketClick: () => void;
}

export function DurationsPanel({buckets, onBucketClick}: DurationsPanelProps) {
    const total = buckets.reduce((acc, bucket) => acc + bucket.count, 0);

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Tests by duration</CardTitle>
            </CardHeader>
            <CardContent>
                <ChartContainer config={CHART_CONFIG} className="aspect-auto h-[150px] w-full">
                    <BarChart data={buckets} margin={{top: 0, right: 0, bottom: 0, left: 0}}>
                        <XAxis dataKey="label" tickLine={false} axisLine={false} tickMargin={6} className="text-[10px]"/>
                        <YAxis hide allowDecimals={false}/>
                        <ChartTooltip content={<ChartTooltipContent/>}/>
                        <Bar dataKey="count" maxBarSize={36} radius={2} fill={SERIES_COLOURS[0]} onClick={onBucketClick} isAnimationActive={false} className="cursor-pointer"/>
                    </BarChart>
                </ChartContainer>
                <div className="flex flex-wrap items-center gap-1 pt-2">
                    {buckets.map(bucket => (
                        <button
                            key={bucket.label}
                            type="button"
                            onClick={onBucketClick}
                            className="rounded border border-border px-1 font-mono text-[10px] text-muted-foreground hover:bg-accent hover:text-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                        >
                            {bucket.label} {bucket.count}
                        </button>
                    ))}
                </div>
                <p className="pt-2 text-[10px] text-muted-foreground">{total} timed tests</p>
            </CardContent>
        </Card>
    );
}
