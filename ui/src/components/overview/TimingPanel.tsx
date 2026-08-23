import {Area, AreaChart, XAxis, YAxis} from "recharts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {TimingModel} from "@/lib/overviewTiming";
import {formatMs} from "@/util/formatMs";
import {PANEL_TITLE_CLASS, SERIES_COLOURS} from "./chartConfig";
import {StatTile} from "./StatTile";

const CHART_CONFIG = {
    running: {label: 'Running', color: SERIES_COLOURS[0]},
} satisfies ChartConfig;

interface TimingPanelProps {
    timing: TimingModel;
}

export function TimingPanel({timing}: TimingPanelProps) {
    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Timing &middot; whole run</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-wrap items-center gap-8">
                <StatTile label="wall clock" value={formatMs(timing.wallClockMs)}/>
                <StatTile label="total elapsed" value={formatMs(timing.totalElapsedMs)}/>
                <StatTile
                    label={timing.speedup > 1 ? `parallel speed-up · ${formatMs(timing.savedMs)} saved` : 'no parallel speed-up'}
                    value={timing.speedup > 1 ? `${timing.speedup.toFixed(1)}×` : 'sequential'}
                    tone={timing.speedup > 1.05 ? 'success' : 'default'}
                />
                <StatTile label="peak concurrent tests" value={`${timing.peak}`}/>
                <div className="min-w-[240px] flex-1">
                    <p className="pb-1 text-[10px] text-muted-foreground">concurrency over wall clock</p>
                    <ChartContainer config={CHART_CONFIG} className="aspect-auto h-[90px] w-full">
                        <AreaChart data={timing.steps} margin={{top: 0, right: 0, bottom: 0, left: 0}}>
                            <XAxis dataKey="t" type="number" domain={[0, timing.wallClockMs]} tickFormatter={formatMs} tickLine={false} axisLine={false} tickMargin={4} className="text-[10px]"/>
                            <YAxis allowDecimals={false} width={24} tickLine={false} axisLine={false} tickMargin={2} className="text-[10px]"/>
                            <ChartTooltip content={<ChartTooltipContent labelFormatter={label => formatMs(Number(label))}/>}/>
                            <Area type="stepAfter" dataKey="running" stroke={SERIES_COLOURS[0]} strokeWidth={1} fill={SERIES_COLOURS[0]} fillOpacity={0.25} isAnimationActive={false}/>
                        </AreaChart>
                    </ChartContainer>
                </div>
            </CardContent>
        </Card>
    );
}
