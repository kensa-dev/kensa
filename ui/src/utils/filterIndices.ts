import {matchesAnyIssue} from "@/util/issueMatch";
import {tagMatch} from "@/util/tagMatch";
import {QueryMeta} from "@/util/queryMeta";
import {Index, Indices} from "@/types/Index";

export interface FilterResult {
    filteredIndices: Indices;
    firstMatchingTest: Index | null;
    firstMatchingMethod: string | null;
    testMethodMap: Map<string, string | null>;
    matchingMethodsMap: Map<string, string[]>;
}

// Report states carry spaces ("Not Executed") but filter terms cannot, so both
// sides are compared with whitespace stripped: `state:notexecuted` matches
// "Not Executed" and `state:passed` still matches "Passed".
const normaliseState = (state: string | undefined | null): string => (state || "").toLowerCase().replace(/\s+/g, '');

export function filterIndices(indices: Indices, queryMeta: QueryMeta, inputValue: string): FilterResult {
    const {states, issues, epics, tags, packages, text: committedText} = queryMeta;

    const activeTyping = inputValue.toLowerCase();
    const typingState = activeTyping.startsWith('state:') ? activeTyping.split(':')[1] : null;
    const typingIssue = activeTyping.startsWith('issue:') ? activeTyping.split(':')[1] : null;
    const typingEpic = activeTyping.startsWith('epic:') ? activeTyping.split(':')[1] : null;
    const typingTag = activeTyping.startsWith('tag:') && activeTyping.length > 4 ? activeTyping.slice(4) : null;
    const typingPkg = activeTyping.startsWith('pkg:') && activeTyping.length > 4 ? activeTyping.slice(4) : null;
    const typingText = (!typingState && !typingIssue && !typingEpic && !typingTag && !typingPkg) ? activeTyping : "";

    const requiredStates = typingState ? [...states, typingState] : states;
    const requiredPackages = typingPkg ? [...packages, typingPkg] : packages;
    const requiredIssues = typingIssue ? [...issues, typingIssue] : issues;
    const requiredEpics = typingEpic ? [...epics, typingEpic] : epics;
    const requiredTags = new Set(typingTag ? [...tags, typingTag] : tags);

    let firstTest: Index | null = null;
    let firstMethod: string | null = null;
    const methodMap = new Map<string, string | null>();
    const allMatchingMethodsMap = new Map<string, string[]>();

    const filterNode = (node: Index): Index | null => {
        // Sysview / overview entries are navigation affordances, not tests — always preserve
        // regardless of search / state / issue filters. Without this short-circuit
        // they fall through to the `return null` at the bottom because testClass is
        // '' (so isLeaf is false) and they have no children to recurse into.
        if (node.type === 'system-view' || node.type === 'overview') return node;

        const isLeaf = node.testClass && (!node.children || node.children.every((c: Index) => c.testMethod));

        if (isLeaf) {
            const nodeName = (node.displayName || "").toLowerCase();
            const nodeClass = (node.testClass || "").toLowerCase();
            const nodeState = normaliseState(node.state);
            const nodeIssues = (node.issues || []).map((i: string) => i.toLowerCase());
            const nodeEpics = (node.epics || []).map(e => e.toLowerCase());

            const matchesText =
                (!committedText || nodeName.includes(committedText) || nodeClass.includes(committedText)) &&
                (!typingText || nodeName.includes(typingText) || nodeClass.includes(typingText));
            const matchesPackage = requiredPackages.length === 0 ||
                requiredPackages.some(pkg => nodeClass.startsWith(pkg.toLowerCase()));

            const matchesState = requiredStates.length === 0 ||
                requiredStates.some(s => nodeState.startsWith(normaliseState(s)));

            // Class issues...
            const classMatchesIssue = requiredIssues.length === 0 ||
                matchesAnyIssue(nodeIssues, requiredIssues);

            const classMatchesEpic = requiredEpics.length === 0 || matchesAnyIssue(nodeEpics, requiredEpics);

            const classMatchesTag = tagMatch(node.tags, requiredTags);

            // If we have state/issue/epic/tag filters, we MUST filter children
            if (node.children && (requiredStates.length > 0 || requiredIssues.length > 0 || requiredEpics.length > 0 || requiredTags.size > 0)) {
                const matchingChildren = node.children.filter((child: Index) => {
                    const childState = normaliseState(child.state);
                    const childMatchesState = requiredStates.length === 0 ||
                        requiredStates.some(s => childState.startsWith(normaliseState(s)));

                    const childMatchesIssueFilter = requiredIssues.length === 0
                        ? true
                        : (classMatchesIssue || matchesAnyIssue(child.issues || [], requiredIssues));

                    const childMatchesEpicFilter = requiredEpics.length === 0
                        ? true
                        : (classMatchesEpic || matchesAnyIssue(child.epics || [], requiredEpics));

                    const childMatchesTagFilter = requiredTags.size === 0
                        ? true
                        : (classMatchesTag || tagMatch(child.tags, requiredTags));

                    return childMatchesState && childMatchesIssueFilter && childMatchesEpicFilter && childMatchesTagFilter;
                });

                if (matchingChildren.length > 0 && matchesText && matchesPackage) {
                    const matchingMethod = matchingChildren[0]?.testMethod || null;
                    const allMethods = matchingChildren.map(c => c.testMethod).filter((m): m is string => Boolean(m));
                    methodMap.set(node.id, matchingMethod);
                    allMatchingMethodsMap.set(node.id, allMethods);
                    if (!firstTest) {
                        firstTest = node;
                        firstMethod = matchingMethod;
                    }
                    return {...node, children: matchingChildren};
                }
                // If no children match the state/issue/epic filter, don't show this node
                return null;
            }

            // No state/issue/epic/tag filters - use class-level matching
            if (matchesText && matchesPackage && matchesState && classMatchesIssue && classMatchesEpic && classMatchesTag) {
                methodMap.set(node.id, null);
                allMatchingMethodsMap.set(node.id, []);
                if (!firstTest) firstTest = node;
                return node;
            }

            return null;
        }

        if (node.children) {
            const filteredChildren: Indices = node.children
                .map((child: Index) => filterNode(child))
                .filter((child): child is Index => Boolean(child));

            if (filteredChildren.length > 0) {
                const firstChildMethod = filteredChildren[0]?.testMethod || null;
                const allMethods = filteredChildren.map(c => c.testMethod).filter((m): m is string => Boolean(m));
                if (firstChildMethod && node.id) {
                    methodMap.set(node.id, firstChildMethod);
                    allMatchingMethodsMap.set(node.id, allMethods);
                    if (!firstTest) {
                        firstTest = node;
                        firstMethod = firstChildMethod;
                    }
                }
                return {...node, children: filteredChildren};
            }
        }

        return null;
    };

    const filtered = indices
        .map(idx => filterNode(idx))
        .filter((idx): idx is Index => Boolean(idx));

    return {
        filteredIndices: filtered,
        firstMatchingTest: firstTest,
        firstMatchingMethod: firstMethod,
        testMethodMap: methodMap,
        matchingMethodsMap: allMatchingMethodsMap
    };
}
