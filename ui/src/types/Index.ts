import {TestState} from "@/types/Test";

export type NodeType = 'project' | 'package' | 'test' | 'system-view'

export interface Index {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
    type?: NodeType;
    issues?: string[];
    children?: Indices;
    testMethod?: string;
    hasErrors?: boolean;
    sourceId?: string;
}

export type Indices = Index[]

export interface SelectedIndex {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
    sourceId?: string;
}
