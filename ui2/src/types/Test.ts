import {TabType} from "@/constants.ts"

export type NameAndValue = Record<string, string>
export type NameAndValues = NameAndValue[]

export interface Token {
    types: string[]
    value: string
    hint: string
    // For nested sentences...
    parameterTokens: Token[]
    tokens: Token[]
}

export interface Sentence {
    tokens: Token[]
}

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
    values: RenderedInteractionValue[] // TODO - shouldn't be an array as there can be only one (fix JsonTransforms)
    attributes: NamedAttributes[]
}

export interface Interaction {
    id: string
    name: string
    rendered: RenderedInteraction
    attributes: NameAndValues
}

export interface Invocation {
    state: string
    displayName: string
    elapsedTime: string
    highlights: string[]
    sentences: Sentence[]
    parameters: NameAndValues
    givens: NameAndValues
    capturedInteractions: Interaction[]
    capturedOutputs: NameAndValues
    fixtures: NameAndValues
    sequenceDiagram: string
    executionException: Record<string, string>
}

export interface Test {
    elapsedTime: string
    testMethod: string
    displayName: string
    notes: string | null
    issues: string[]
    state: 'Passed' | 'Failed'
    autoOpenTab?: TabType
    invocations: Invocation[]
}

export interface TestDetail {
    testClass: string
    displayName: string
    state: 'Passed' | 'Failed'
    notes: string | null
    minimumUniquePackageName: string
    issues: string[]
    tests: Test[]
}