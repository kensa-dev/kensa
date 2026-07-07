export interface FixtureSubstitution {
    value: string;
    name: string;
}

// Replaces every occurrence of each substitution's concrete value with a
// <fixture-name> placeholder. split/join keeps values literal (no regex).
export function substitutePayloadValue(payload: string, substitutions: FixtureSubstitution[]): string {
    return substitutions.reduce(
        (acc, {value, name}) => (value ? acc.split(value).join(`<${name}>`) : acc),
        payload,
    );
}
