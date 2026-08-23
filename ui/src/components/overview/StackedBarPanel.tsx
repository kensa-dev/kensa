import {Bar, BarChart, XAxis, YAxis} from "recharts";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {ChartContainer, ChartTooltip, ChartTooltipContent} from "@/components/ui/chart";
import {GroupCounts} from "@/lib/overview";
import {PANEL_TITLE_CLASS, RESULT_CHART_CONFIG, RESULT_KEYS} from "./chartConfig";

const MAX_GROUPS = 12;
const MIN_HEIGHT = 120;
const ROW_HEIGHT = 22;

interface StackedBarPanelProps {
    title: string;
    groups: GroupCounts[];
    prefix: 'tag:' | 'pkg:';
    onFilter: (term: string) => void;
}

function commonSegmentCount(keys: string[]): number {
    if (keys.length < 2) return 0;
    const split = keys.map(key => key.split('.'));
    const shortest = split.reduce((a, b) => a.length <= b.length ? a : b);
    let common = 0;
    while (common < shortest.length - 1 && split.every(parts => parts[common] === shortest[common])) common++;
    return common;
}

interface BarClickPayload {
    key?: string;
    payload?: {key?: string};
}

export function StackedBarPanel({title, groups, prefix, onFilter}: StackedBarPanelProps) {
    const shown = groups.slice(0, MAX_GROUPS);
    const hidden = groups.length - shown.length;
    const activeKeys = RESULT_KEYS.filter(key => shown.some(group => group[key] > 0));
    const height = Math.max(MIN_HEIGHT, shown.length * ROW_HEIGHT);
    const bandHeight = height / shown.length;
    const trimmed = commonSegmentCount(shown.map(group => group.key));

    const onBarClick = (bar: BarClickPayload) => {
        const key = bar.payload?.key ?? bar.key;
        // The default package has an empty key, so there is no term that could filter on it.
        if (key) onFilter(prefix + key);
    };

    return (
        <Card className="h-full">
            <CardHeader className="pb-2">
                <CardTitle className={PANEL_TITLE_CLASS}>{title}</CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex gap-2">
                    <div className="flex w-[120px] shrink-0 flex-col" style={{height}}>
                        {shown.map(group => group.key === '' ? (
                            <div
                                key={group.key}
                                style={{height: bandHeight}}
                                className="-mx-1 flex items-center justify-between gap-1 rounded px-1 text-left text-[11px] text-muted-foreground"
                                title="(default package)"
                            >
                                <span className="truncate">(default package)</span>
                                <span className="font-mono tabular-nums text-muted-foreground">{group.total}</span>
                            </div>
                        ) : (
                            <button
                                key={group.key}
                                type="button"
                                onClick={() => onFilter(prefix + group.key)}
                                style={{height: bandHeight}}
                                className="-mx-1 flex items-center justify-between gap-1 rounded px-1 text-left text-[11px] hover:bg-accent focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                                title={group.key}
                            >
                                <span className="truncate">{group.key.split('.').slice(trimmed).join('.')}</span>
                                <span className="font-mono tabular-nums text-muted-foreground">{group.total}</span>
                            </button>
                        ))}
                    </div>
                    <ChartContainer config={RESULT_CHART_CONFIG} className="aspect-auto flex-1" style={{height}}>
                        <BarChart data={shown} layout="vertical" margin={{top: 0, right: 0, bottom: 0, left: 0}} barCategoryGap="25%">
                            <YAxis dataKey="key" type="category" hide/>
                            <XAxis type="number" hide/>
                            <ChartTooltip content={<ChartTooltipContent/>}/>
                            {activeKeys.map(key => (
                                <Bar key={key} dataKey={key} stackId="a" maxBarSize={12} fill={`var(--color-${key})`} onClick={onBarClick} isAnimationActive={false} className="cursor-pointer"/>
                            ))}
                        </BarChart>
                    </ChartContainer>
                </div>
                <div className="flex flex-wrap items-center gap-3 pt-2 text-[10px] text-muted-foreground">
                    {activeKeys.map(key => (
                        <span key={key} className="flex items-center gap-1">
                            <span className="h-2 w-2 rounded-[2px]" style={{background: RESULT_CHART_CONFIG[key].color}}/>
                            {RESULT_CHART_CONFIG[key].label}
                        </span>
                    ))}
                    {hidden > 0 && <span className="ml-auto">+{hidden} more</span>}
                </div>
            </CardContent>
        </Card>
    );
}
