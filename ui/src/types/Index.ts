import {TestState} from "@/types/Test";

export type NodeType = 'project' | 'package' | 'test' | 'system-view' | 'overview'

export interface Index {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
    type?: NodeType;
    issues?: string[];
    epics?: string[];
    tags?: string[];
    children?: Indices;
    testMethod?: string;
    hasErrors?: boolean;
    sourceId?: string;
    timing?: [number, number][];
    interactions?: number;
    participants?: Record<string, number>;
    assertions?: number;
    expandables?: number;
}

export type Indices = Index[]

export interface SelectedIndex {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
    sourceId?: string;
}
