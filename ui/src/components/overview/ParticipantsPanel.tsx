import {Bar, BarChart, XAxis, YAxis} from "recharts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartConfig, ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {GroupCount} from "@/lib/overview";
import {PANEL_TITLE_CLASS, SERIES_COLOURS} from "./chartConfig";

const MAX_GROUPS = 8;
const MIN_HEIGHT = 120;
const ROW_HEIGHT = 24;

const CHART_CONFIG = {
    count: {label: 'Messages', color: SERIES_COLOURS[1]},
} satisfies ChartConfig;

interface ParticipantsPanelProps {
    groups: GroupCount[];
}

export function ParticipantsPanel({groups}: ParticipantsPanelProps) {
    const shown = groups.slice(0, MAX_GROUPS);
    const hidden = groups.length - shown.length;
    const messages = groups.reduce((acc, group) => acc + group.count, 0);
    const height = Math.max(MIN_HEIGHT, shown.length * ROW_HEIGHT);

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>Interactions by participant</CardTitle>
            </CardHeader>
            <CardContent>
                <ChartContainer config={CHART_CONFIG} className="aspect-auto w-full" style={{height}}>
                    <BarChart data={shown} layout="vertical" margin={{top: 0, right: 0, bottom: 0, left: 0}} barCategoryGap="25%">
                        <YAxis dataKey="key" type="category" width={110} tickLine={false} axisLine={false} tickMargin={4} className="text-[10px]"/>
                        <XAxis type="number" hide allowDecimals={false}/>
                        <ChartTooltip content={<ChartTooltipContent/>}/>
                        <Bar dataKey="count" maxBarSize={12} radius={2} fill={SERIES_COLOURS[1]} isAnimationActive={false}/>
                    </BarChart>
                </ChartContainer>
                <p className="pt-2 text-[10px] text-muted-foreground">
                    {messages} messages captured
                    {hidden > 0 && <span> · +{hidden} more participants</span>}
                </p>
            </CardContent>
        </Card>
    );
}
