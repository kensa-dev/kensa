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
    tests: Test[];
    issues?: string[];
}

export type Indices = Index[]

export interface SelectedIndex {
    id: string;
    displayName: string;
    testClass: string;
    state: 'Passed' | 'Failed';
}
