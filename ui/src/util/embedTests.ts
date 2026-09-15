// The tests an embed shows: the one named by `method` (by method name or
// display name, as TestContainer matches them), or the whole class without one.
export interface EmbedTest {
    testMethod: string;
    displayName: string;
}

const normalise = (s: string) => s.trim().toLowerCase();

export function embedTests<T extends EmbedTest>(tests: T[], method: string | null): T[] {
    if (method === null) return tests;
    const target = normalise(method);
    return tests.filter(t => normalise(t.testMethod) === target || normalise(t.displayName) === target);
}
