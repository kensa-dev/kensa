import type { FixtureSpec, NameAndValues } from '@/types/Test';

const keyForValue = (value: string, fixtures: NameAndValues): string | undefined => {
    const entry = fixtures.find(nv => Object.values(nv)[0] === value);
    return entry ? Object.keys(entry)[0] : undefined;
};

export const isRelatedFixture = (
    selectedValue: string,
    thisValue: string,
    fixtures: NameAndValues,
    specs: FixtureSpec[]
): boolean => {
    if (selectedValue === thisValue) return false;
    const selectedKey = keyForValue(selectedValue, fixtures);
    const thisKey = keyForValue(thisValue, fixtures);
    if (!selectedKey || !thisKey) return false;
    const selectedSpec = specs.find(s => s.key === selectedKey);
    const thisSpec = specs.find(s => s.key === thisKey);
    if (!selectedSpec || !thisSpec) return false;
    return thisSpec.parents.includes(selectedKey) || selectedSpec.parents.includes(thisKey);
};
