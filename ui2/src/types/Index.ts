import {TestState} from "@/types/Test.ts";

export type NodeType = 'project' | 'package' | 'test'

export interface Index {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
    type?: NodeType;
    issues?: string[];
    children?: Indices;
    testMethod?: string;
}

export type Indices = Index[]

export interface SelectedIndex {
    id: string;
    displayName: string;
    testClass: string;
    state: TestState;
}
