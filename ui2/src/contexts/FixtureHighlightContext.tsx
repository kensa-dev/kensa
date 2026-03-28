import {createContext, useContext, useState, ReactNode} from 'react';
import {FixtureSpec, NameAndValues} from '@/types/Test';

interface FixtureHighlightState {
    selected: string | undefined
    setSelected: (value: string | undefined) => void
    fixtureSpecs: FixtureSpec[]
    fixtures: NameAndValues
}

const FixtureHighlightContext = createContext<FixtureHighlightState>({
    selected: undefined,
    setSelected: () => undefined,
    fixtureSpecs: [],
    fixtures: [],
});

export const useFixtureHighlight = () => useContext(FixtureHighlightContext);

interface FixtureHighlightProviderProps {
    fixtureSpecs: FixtureSpec[]
    fixtures: NameAndValues
    children: ReactNode
}

export const FixtureHighlightProvider = ({fixtureSpecs, fixtures, children}: FixtureHighlightProviderProps) => {
    const [selected, setSelected] = useState<string | undefined>(undefined);

    return (
        <FixtureHighlightContext.Provider value={{selected, setSelected, fixtureSpecs, fixtures}}>
            {children}
        </FixtureHighlightContext.Provider>
    );
};
