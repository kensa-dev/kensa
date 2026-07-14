import type { NameAndValues } from '@/types/Test';

export type InvocationSummary =
    | { kind: 'displayName'; text: string }
    | { kind: 'empty'; text: 'Invocation' }
    | { kind: 'inline'; text: string }
    | { kind: 'chip'; count: number; text: string };

export const INLINE_MAX_PARAMS = 3;
export const INLINE_MAX_LENGTH = 80;

const LEADING_INDEX = /^\[\d+]\s+/;

/** JUnit5 (and similar) prefix parameterised display names with "[N] ". The
 *  invocation card already renders an explicit #N index badge, so drop the
 *  redundant framework prefix to avoid showing both.
 *
 *  The trailing whitespace is required (JUnit's default is "[{index}] {args}"),
 *  so a bare value like a Kotest "[123]" list toString is left untouched. */
export function stripLeadingIndex(name: string): string {
    return name.replace(LEADING_INDEX, '');
}

export function summarizeInvocation(invocation: {
    displayName?: string;
    parameters: NameAndValues;
}): InvocationSummary {
    if (invocation.displayName) {
        const text = stripLeadingIndex(invocation.displayName);
        if (text) {
            return { kind: 'displayName', text };
        }
    }
    const count = invocation.parameters.length;
    if (count === 0) {
        return { kind: 'empty', text: 'Invocation' };
    }
    const joined = invocation.parameters
        .map((p) => {
            const [name] = Object.keys(p);
            return `${name}: ${p[name]}`;
        })
        .join(', ');
    if (count <= INLINE_MAX_PARAMS && joined.length <= INLINE_MAX_LENGTH) {
        return { kind: 'inline', text: joined };
    }
    return { kind: 'chip', count, text: joined };
}
