export function matchesAnyIssue(nodeIssues: string[], requiredIssues: string[]): boolean {
    const required = requiredIssues.map(i => i.toLowerCase());
    const have = nodeIssues.map(i => i.toLowerCase());
    return have.some(ni => required.includes(ni));
}
