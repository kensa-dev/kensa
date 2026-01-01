export const Section = {
    Tabs: 'Tabs',
    Exception: 'Exception',
    Sentences: 'Sentences'
} as const;

export const Tab = {
    CapturedOutputs: 'capturedOutputs',
    CapturedInteractions: 'capturedInteractions',
    Fixtures: 'fixtures',
    Givens: 'givens',
    Parameters: 'parameters',
    SequenceDiagram: 'sequenceDiagram',
} as const;

export type SectionType = typeof Section[keyof typeof Section];
export type TabType = typeof Tab[keyof typeof Tab];