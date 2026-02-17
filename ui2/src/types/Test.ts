import {TabType} from "@/constants.ts"

export type NameAndValue = Record<string, string>
export type NameAndValues = NameAndValue[]

export type TestState = "Passed" | "Failed" | "Disabled"

export interface TableData {
    type: 'table'
    headers?: string[]
    rows: Token[][]
}

export interface Token {
    types: string[]
    value: string
    hint?: string
    parameterTokens?: Token[]
    tokens?: NestedItem[]
}

export type NestedItem = Token[] | TableData;

export interface NamedAttributes {
    name: string
    attributes: NameAndValues
}

export interface RenderedInteractionValue {
    name: string
    value: string
    language: string
}

export interface RenderedInteraction {
    values: RenderedInteractionValue[]
    attributes: NamedAttributes[]
}

export interface Interaction {
    id: string
    name: string
    from?: string | null
    to?: string | null
    rendered: RenderedInteraction
    attributes: NameAndValues
}

export interface CustomTabContent {
    tabId: string
    label: string
    file: string
    mediaType?: string
}

export interface Invocation {
    state: TestState
    displayName: string
    elapsedTime: string
    highlights: string[]
    sentences: Token[][]
    parameters: NameAndValues
    givens: NameAndValues
    capturedInteractions: Interaction[]
    capturedOutputs: NameAndValues
    fixtures: NameAndValues
    sequenceDiagram?: string
    executionException: Record<string, string>
    customTabContents?: CustomTabContent[]
}

export interface Test {
    elapsedTime: string
    testMethod: string
    displayName: string
    notes: string | null
    issues: string[]
    state: TestState
    autoOpenTab?: TabType
    invocations: Invocation[]
}

export interface TestDetail {
    testClass: string
    displayName: string
    state: TestState
    notes: string | null
    minimumUniquePackageName: string
    issues: string[]
    tests: Test[]
}