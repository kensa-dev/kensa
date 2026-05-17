import {createContext, useContext, ReactNode} from 'react';

type TagClickHandler = (tag: string, additive: boolean) => void;

interface TagFilterState {
    onTagClick: TagClickHandler;
    selectedTags: Set<string>;
}

const TagFilterContext = createContext<TagFilterState>({
    onTagClick: () => undefined,
    selectedTags: new Set(),
});

export const useTagFilter = () => useContext(TagFilterContext);

interface TagFilterProviderProps {
    onTagClick: TagClickHandler;
    selectedTags: Set<string>;
    children: ReactNode;
}

export const TagFilterProvider = ({onTagClick, selectedTags, children}: TagFilterProviderProps) => (
    <TagFilterContext.Provider value={{onTagClick, selectedTags}}>
        {children}
    </TagFilterContext.Provider>
);
