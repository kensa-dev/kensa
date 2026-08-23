import {Indices} from '@/types/Index';

export interface ConcurrencyStep { t: number; running: number }

export interface TimingModel {
    wallClockMs: number;
    totalElapsedMs: number;
    speedup: number;
    savedMs: number;
    peak: number;
    steps: ConcurrencyStep[];
}

const MAX_STEPS = 200;

export function collectIntervals(nodes: Indices): [number, number][] {
    const out: [number, number][] = [];
    const walk = (list: Indices) => {
        for (const n of list) {
            if (n.timing) out.push(...n.timing);
            if (n.children) walk(n.children);
        }
    };
    walk(nodes);
    return out;
}

export function computeTiming(intervals: [number, number][]): TimingModel | null {
    if (intervals.length === 0) return null;
    const runStart = Math.min(...intervals.map(([s]) => s));
    const runEnd = Math.max(...intervals.map(([s, e]) => s + e));
    const totalElapsedMs = intervals.reduce((acc, [, e]) => acc + e, 0);
    const wallClockMs = Math.max(runEnd - runStart, 1);

    const events: {t: number; delta: number}[] = [];
    for (const [s, e] of intervals) {
        events.push({t: s - runStart, delta: 1});
        events.push({t: s - runStart + e, delta: -1});
    }
    events.sort((a, b) => a.t - b.t || a.delta - b.delta);

    let running = 0;
    let peak = 0;
    const raw: ConcurrencyStep[] = [];
    for (const ev of events) {
        running += ev.delta;
        peak = Math.max(peak, running);
        raw.push({t: ev.t, running});
    }

    const steps = raw.length <= MAX_STEPS ? raw : bucket(raw, wallClockMs);
    return {wallClockMs, totalElapsedMs, speedup: totalElapsedMs / wallClockMs, savedMs: totalElapsedMs - wallClockMs, peak, steps};
}

function bucket(raw: ConcurrencyStep[], wallClockMs: number): ConcurrencyStep[] {
    const width = wallClockMs / MAX_STEPS;
    const maxByBucket = new Map<number, number>();
    for (const s of raw) {
        const b = Math.min(Math.floor(s.t / width), MAX_STEPS - 1);
        maxByBucket.set(b, Math.max(maxByBucket.get(b) ?? 0, s.running));
    }
    return [...maxByBucket.entries()].sort((a, b) => a[0] - b[0]).map(([b, running]) => ({t: Math.round(b * width), running}));
}
