export function tagMatch(testTags: string[] | undefined, selectedTags: Set<string>): boolean {
    if (selectedTags.size === 0) return true;
    if (!testTags || testTags.length === 0) return false;
    return testTags.some(t => selectedTags.has(t));
}
