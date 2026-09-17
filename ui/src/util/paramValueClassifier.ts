import type {ReportedParameterKind} from '@/types/Test';

export type ParamValueKind = 'number' | 'boolean' | 'null' | 'string' | 'json' | 'other';

export interface ClassifiedValue {
    kind: ParamValueKind;
    displayValue: string;
    className: string;
}

const NULL_CLASS = 'text-muted-foreground/70 italic';
const NUMBER_CLASS = 'text-blue-500';
const BOOLEAN_CLASS = 'text-orange-500';
const JSON_CLASS = 'text-emerald-500/90 font-mono whitespace-pre-wrap';
const STRING_CLASS = 'text-emerald-600';

const NUMBER_RE = /^-?\d+(\.\d+)?$/;

function classifyAsString(raw: string): ClassifiedValue {
    if (raw.startsWith('{') || raw.startsWith('[')) {
        try {
            const parsed = JSON.parse(raw);
            return { kind: 'json', displayValue: JSON.stringify(parsed, null, 2), className: JSON_CLASS };
        } catch {
            return { kind: 'string', displayValue: raw, className: STRING_CLASS };
        }
    }
    const alreadyQuoted = raw.startsWith('"') && raw.endsWith('"');
    const displayValue = alreadyQuoted ? raw : `"${raw}"`;
    return { kind: 'string', displayValue, className: STRING_CLASS };
}

export function classifyParamValue(raw: string, kind?: ReportedParameterKind): ClassifiedValue {
    if (kind === 'null') {
        return { kind: 'null', displayValue: 'null', className: NULL_CLASS };
    }
    if (kind === 'number') {
        return { kind: 'number', displayValue: raw, className: NUMBER_CLASS };
    }
    if (kind === 'boolean') {
        return { kind: 'boolean', displayValue: raw, className: BOOLEAN_CLASS };
    }
    if (kind === 'string') {
        return classifyAsString(raw);
    }

    if (raw === 'null' || raw === 'undefined') {
        return { kind: 'null', displayValue: 'null', className: NULL_CLASS };
    }
    if (NUMBER_RE.test(raw)) {
        return { kind: 'number', displayValue: raw, className: NUMBER_CLASS };
    }
    if (raw === 'true' || raw === 'false') {
        return { kind: 'boolean', displayValue: raw, className: BOOLEAN_CLASS };
    }
    return classifyAsString(raw);
}
