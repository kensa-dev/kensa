export interface Test {
    displayName: string;
    testMethod: string;
    state: 'Passed' | 'Failed';
    issues?: string[];
}

export interface Index {
    id: string;
    displayName: string;
    testClass: string;
    state: 'Passed' | 'Failed';
    type?: 'project' | 'package' | 'test';
    issues?: string[];
    children?: Index[];
    testMethod?: string;
}

export type Indices = Index[]

export interface SelectedIndex {
    id: string;
    displayName: string;
    testClass: string;
    state: 'Passed' | 'Failed';
}
