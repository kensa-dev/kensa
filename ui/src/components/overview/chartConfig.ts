import {ChartConfig} from "@/components/ui/chart";

export type ResultKey = 'passed' | 'failed' | 'disabled' | 'notExecuted';

export const RESULT_KEYS: ResultKey[] = ['passed', 'failed', 'disabled', 'notExecuted'];

export const RESULT_CHART_CONFIG = {
    passed: {label: 'Passed', color: 'hsl(var(--success))'},
    failed: {label: 'Failed', color: 'hsl(var(--failure))'},
    disabled: {label: 'Disabled', color: 'hsl(var(--disabled))'},
    notExecuted: {label: 'Not executed', color: 'hsl(var(--muted-foreground))'},
} satisfies ChartConfig;

export const RESULT_FILTER_TERMS: Record<ResultKey, string> = {
    passed: 'state:passed',
    failed: 'state:failed',
    disabled: 'state:disabled',
    notExecuted: 'state:notexecuted',
};

export const RESULT_TEXT_CLASSES: Record<ResultKey, string> = {
    passed: 'text-success',
    failed: 'text-failure',
    disabled: 'text-disabled',
    notExecuted: 'text-muted-foreground',
};

export const RESULT_BAR_CLASSES: Record<ResultKey, string> = {
    passed: 'bg-success/80',
    failed: 'bg-failure/80',
    disabled: 'bg-disabled/80',
    notExecuted: 'bg-muted-foreground/80',
};

export const SERIES_COLOURS: string[] = [
    'hsl(var(--chart-1))',
    'hsl(var(--chart-2))',
    'hsl(var(--chart-3))',
    'hsl(var(--chart-4))',
    'hsl(var(--chart-5))',
];

export const PANEL_TITLE_CLASS = "text-[11px] font-semibold uppercase tracking-wider text-muted-foreground";

export const simpleName = (testClass: string): string => testClass.split('.').pop() ?? testClass;
