import type { NameAndValues } from '@/types/Test';

export type InvocationSummary =
    | { kind: 'displayName'; text: string }
    | { kind: 'empty'; text: 'Invocation' }
    | { kind: 'inline'; text: string }
    | { kind: 'chip'; count: number };

export const INLINE_MAX_PARAMS = 3;
export const INLINE_MAX_LENGTH = 80;

export function summarizeInvocation(invocation: {
    displayName?: string;
    parameters: NameAndValues;
}): InvocationSummary {
    if (invocation.displayName) {
        return { kind: 'displayName', text: invocation.displayName };
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
    return { kind: 'chip', count };
}
