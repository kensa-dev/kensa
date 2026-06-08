import type { MergedIndex } from '@/lib/suiteSearch';
import type { NameAndValues } from '@/types/Test';

export function fixtureNamesForValue(
    value: string,
    index: MergedIndex,
    fixtures: NameAndValues,
): string[] {
    const term = index.terms.find((t) => t.value === value);
    if (term && term.names.length > 0) return term.names;

    const names: string[] = [];
    for (const entry of fixtures) {
        for (const [name, fixtureValue] of Object.entries(entry)) {
            if (fixtureValue === value && !names.includes(name)) names.push(name);
        }
    }
    return names;
}
