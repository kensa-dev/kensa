import {createContext, useContext, ReactNode} from 'react';
import {FilterPrefix} from "@/util/tagClick";

type TagClickHandler = (tag: string, additive: boolean) => void;
type FilterClickHandler = (prefix: FilterPrefix, value: string, additive: boolean) => void;

interface TagFilterState {
    onTagClick: TagClickHandler;
    onFilterClick: FilterClickHandler;
    selectedTags: Set<string>;
    query: string;
}

const TagFilterContext = createContext<TagFilterState>({
    onTagClick: () => undefined,
    onFilterClick: () => undefined,
    selectedTags: new Set(),
    query: '',
});

export const useTagFilter = () => useContext(TagFilterContext);

interface TagFilterProviderProps {
    onTagClick: TagClickHandler;
    onFilterClick: FilterClickHandler;
    selectedTags: Set<string>;
    query: string;
    children: ReactNode;
}

export const TagFilterProvider = ({onTagClick, onFilterClick, selectedTags, query, children}: TagFilterProviderProps) => (
    <TagFilterContext.Provider value={{onTagClick, onFilterClick, selectedTags, query}}>
        {children}
    </TagFilterContext.Provider>
);
