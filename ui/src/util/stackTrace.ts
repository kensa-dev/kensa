export const STDLIB_SKIP = /\s+at (java\.|kotlin\.|org\.junit\.|dev\.kensa\.(parse|state|context|output|sentence|render|junit|kotest|testng))/;

export function getFailingLine(stackTrace: string, sentenceLines: Set<number>, testClass: string): number | undefined {
    const classPrefix = `\tat ${testClass}`;
    let result: number | undefined;
    for (const line of stackTrace.split('\n')) {
        if (!line.includes('\tat ') || STDLIB_SKIP.test(line)) continue;
        if (!line.startsWith(classPrefix)) continue;
        const next = line.charAt(classPrefix.length);
        if (next !== '.' && next !== '$') continue;
        const m = line.match(/:(\d+)\)$/);
        if (m) {
            const n = parseInt(m[1]);
            if (sentenceLines.has(n)) result = n;
        }
    }
    return result;
}
