import {FilterPrefix, selectedFilterValues} from "@/util/tagClick";

export interface FilterMenuEntry {
    key: "filter" | "toggle";
    label: string;
    additive: boolean;
}

export const badgeFilterMenu = (query: string, prefix: Exclude<FilterPrefix, "tag">, value: string): FilterMenuEntry[] => {
    const selected = selectedFilterValues(query, prefix).has(value);

    return [
        {key: "filter", label: `Filter by this ${prefix}`, additive: false},
        {key: "toggle", label: selected ? "Remove from filter" : "Add to filter", additive: true},
    ];
};
