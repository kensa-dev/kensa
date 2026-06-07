import * as React from "react"
import {AlertTriangle, ChevronRight, Diamond, Folder, FolderOpen, Globe, Network, Search, X} from "lucide-react"
import {expandProjectChildren} from "@/utils/treeUtils"
import {cn} from "@/lib/utils"
import {Badge} from "@/components/ui/badge"
import {SidebarContent, SidebarGroup, SidebarGroupLabel, SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarMenuSub,} from "@/components/ui/sidebar"
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from "@/components/ui/select"
import {Index, Indices} from "@/types/Index";
import {Collapsible, CollapsibleContent, CollapsibleTrigger,} from "@/components/ui/collapsible"
import {Popover, PopoverContent, PopoverTrigger,} from "@/components/ui/popover"
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip"
import {Kbd, KbdGroup} from "@/components/ui/kbd"
import {useConfig} from "@/contexts/ConfigContext"
import {matchesAnyIssue} from "@/util/issueMatch"
import {tagMatch} from "@/util/tagMatch"
import {hasOpenDialog} from "@/util/escapeGuard"
import {setStateFilter} from "@/util/stateFilterToggle"
import {useLocation} from "react-router-dom"
import {TreeExpansionProvider, useTreeExpansion} from "@/hooks/useTreeExpansion"
import {SidebarTreeToolbar} from "@/components/SidebarTreeToolbar"

const SourceMetaContext = React.createContext<Record<string, { generatedAt?: string }>>({});

interface StateCounts {
    passed: number;
    failed: number;
    disabled: number;
    total: number;
}

const countStates = (nodes: Indices): StateCounts => {
    const counts: StateCounts = {passed: 0, failed: 0, disabled: 0, total: 0};

    const walk = (items: Indices) => {
        for (const node of items) {
            if (node.testMethod) {
                counts.total++;
                if (node.state === "Passed") counts.passed++;
                else if (node.state === "Failed") counts.failed++;
                else if (node.state === "Disabled") counts.disabled++;
            }
            if (node.children) walk(node.children);
        }
    };

    walk(nodes);
    return counts;
};

const countChildStates = (node: Index): StateCounts => {
    return countStates(node.children ?? []);
};

const FILTER_STATES = ['passed', 'failed', 'disabled'] as const;

const BAR_SEGMENTS = [
    {key: "passed" as const, color: "text-success", bg: "bg-success/5", barCls: "bg-success/80"},
    {key: "failed" as const, color: "text-failure", bg: "bg-failure/5", barCls: "bg-failure/80"},
    {key: "disabled" as const, color: "text-disabled", bg: "bg-disabled/5", barCls: "bg-disabled/80"},
] as const;

export interface TestExplorerProps {
    indices: Indices;
    sourceMetaById?: Record<string, { generatedAt?: string }>;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onSelect: (node: Index, firstMatchingMethod: string | null, allMatchingMethods: string[]) => void;
    selectedId: string | null;
    environment: string;
    onEnvChange: (env: string) => void;
    isNative?: boolean;
    inputRef?: React.RefObject<HTMLInputElement>;
    onFilterApplied?: (firstTest: Index | null, firstMethod: string | null, matchingMethodsMap: Map<string, string[]>) => void;
}

export function TestExplorer({indices, sourceMetaById, searchQuery, onSearchChange, onSelect, selectedId, environment, onEnvChange, isNative, inputRef, onFilterApplied}: TestExplorerProps) {
    const {packageDisplay, packageDisplayRoot} = useConfig();

    const allIssues = React.useMemo(() => {
        const issues = new Set<string>();
        const collect = (nodes: Indices) => {
            nodes.forEach(n => {
                (n.issues || []).forEach((i: string) => issues.add(i));
                if (n.children) collect(n.children);
            });
        };
        collect(indices);
        return Array.from(issues).sort();
    }, [indices]);

    const allTags = React.useMemo(() => {
        const tags = new Set<string>();
        const collect = (nodes: Indices) => {
            nodes.forEach(n => {
                (n.tags || []).forEach((t: string) => tags.add(t));
                if (n.children) collect(n.children);
            });
        };
        collect(indices);
        return Array.from(tags).sort();
    }, [indices]);

    const states = FILTER_STATES;

    const [inputValue, setInputValue] = React.useState("");
    const [showPicker, setShowPicker] = React.useState(false);
    const [pickerType, setPickerType] = React.useState<'state' | 'issue' | 'tag' | null>(null);
    const [pickerIndex, setPickerIndex] = React.useState(0);

    const pickerListRef = React.useRef<HTMLDivElement>(null);
    const inputValueRef = React.useRef(inputValue);
    inputValueRef.current = inputValue;

    React.useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && inputValueRef.current && !hasOpenDialog()) {
                e.preventDefault();
                setInputValue('');
                setShowPicker(false);
            }
        };
        document.addEventListener('keydown', handleEscape, true);
        return () => document.removeEventListener('keydown', handleEscape, true);
    }, []);

    React.useEffect(() => {
        if (showPicker && pickerListRef.current) {
            const items = pickerListRef.current.children;
            if (items[pickerIndex]) {
                (items[pickerIndex] as HTMLElement).scrollIntoView({block: 'nearest'});
            }
        }
    }, [pickerIndex, showPicker]);

    const pickerItems = React.useMemo(() => {
        if (pickerType === 'state') return [...states];
        if (pickerType === 'issue') return allIssues;
        if (pickerType === 'tag') return allTags;
        return [];
    }, [pickerType, states, allIssues, allTags]);

    const handleRemoveBadge = (token: string) => {
        const newQuery = searchQuery
            .split(/\s+/)
            .filter(part => part !== token)
            .join(' ')
            .trim();
        onSearchChange(newQuery);
    };

    const handleRemoveText = () => {
        const newQuery = searchQuery
            .split(/\s+/)
            .filter(p =>
                (p.startsWith('issue:') && p.length > 6) ||
                (p.startsWith('state:') && p.length > 6) ||
                (p.startsWith('tag:') && p.length > 4) ||
                (p.startsWith('pkg:') && p.length > 4)
            )
            .join(' ')
            .trim();
        onSearchChange(newQuery);
    };

    const handleInputChange = (val: string) => {
        setInputValue(val);
        if (val.endsWith('state:')) {
            setPickerType('state');
            setPickerIndex(0);
            setShowPicker(true);
        } else if (val.endsWith('issue:')) {
            setPickerType('issue');
            setPickerIndex(0);
            setShowPicker(true);
        } else if (val.endsWith('tag:')) {
            setPickerType('tag');
            setPickerIndex(0);
            setShowPicker(true);
        } else {
            setShowPicker(false);
        }
    };

    const applySelection = (selectedValue: string) => {
        const token = `${pickerType}:${selectedValue}`;
        onSearchChange(`${searchQuery} ${token}`.trim());
        setInputValue("");
        setShowPicker(false);
        setPickerType(null);

        setTimeout(() => inputRef?.current?.focus(), 0);
    };

    const queryMeta = React.useMemo(() => {
        const parts = searchQuery.split(/\s+/).filter(Boolean);
        const issues = parts.filter(p => p.startsWith('issue:') && p.length > 6).map(p => p.slice(6));
        const states = parts.filter(p => p.startsWith('state:') && p.length > 6).map(p => p.slice(6).toLowerCase());
        const tags = parts.filter(p => p.startsWith('tag:') && p.length > 4).map(p => p.slice(4));
        const packages = parts.filter(p => p.startsWith('pkg:') && p.length > 4).map(p => p.slice(4));

        const text = parts.filter(p =>
            !(p.startsWith('issue:') && p.length > 6) &&
            !(p.startsWith('state:') && p.length > 6) &&
            !(p.startsWith('tag:') && p.length > 4) &&
            !(p.startsWith('pkg:') && p.length > 4)
        ).join(' ');

        return {text, issues, states, tags, packages};
    }, [searchQuery]);

    const {filteredIndices, firstMatchingTest, firstMatchingMethod, testMethodMap, matchingMethodsMap} = React.useMemo(() => {
        const {states, issues, tags, packages, text: committedText} = queryMeta;

        const activeTyping = inputValue.toLowerCase();
        const typingState = activeTyping.startsWith('state:') ? activeTyping.split(':')[1] : null;
        const typingIssue = activeTyping.startsWith('issue:') ? activeTyping.split(':')[1] : null;
        const typingTag = activeTyping.startsWith('tag:') && activeTyping.length > 4 ? activeTyping.slice(4) : null;
        const typingPkg = activeTyping.startsWith('pkg:') && activeTyping.length > 4 ? activeTyping.slice(4) : null;
        const typingText = (!typingState && !typingIssue && !typingTag && !typingPkg) ? activeTyping : "";

        const requiredStates = typingState ? [...states, typingState] : states;
        const requiredPackages = typingPkg ? [...packages, typingPkg] : packages;
        const requiredIssues = typingIssue ? [...issues, typingIssue] : issues;
        const requiredTags = new Set(typingTag ? [...tags, typingTag] : tags);

        let firstTest: Index | null = null;
        let firstMethod: string | null = null;
        const methodMap = new Map<string, string | null>();
        const allMatchingMethodsMap = new Map<string, string[]>();

        const filterNode = (node: Index): Index | null => {
            // Sysview entries are navigation affordances, not tests — always preserve
            // regardless of search / state / issue filters. Without this short-circuit
            // they fall through to the `return null` at the bottom because testClass is
            // '' (so isLeaf is false) and they have no children to recurse into.
            if (node.type === 'system-view') return node;

            const isLeaf = node.testClass && (!node.children || node.children.every((c: Index) => c.testMethod));

            if (isLeaf) {
                const nodeName = (node.displayName || "").toLowerCase();
                const nodeClass = (node.testClass || "").toLowerCase();
                const nodeState = (node.state || "").toLowerCase();
                const nodeIssues = (node.issues || []).map((i: string) => i.toLowerCase());

                const matchesText =
                    (!committedText || nodeName.includes(committedText) || nodeClass.includes(committedText)) &&
                    (!typingText || nodeName.includes(typingText) || nodeClass.includes(typingText));
                const matchesPackage = requiredPackages.length === 0 ||
                    requiredPackages.some(pkg => nodeClass.startsWith(pkg.toLowerCase()));

                const matchesState = requiredStates.length === 0 ||
                    requiredStates.some(s => nodeState.startsWith(s.toLowerCase()));

                // Class issues...
                const classMatchesIssue = requiredIssues.length === 0 ||
                    matchesAnyIssue(nodeIssues, requiredIssues);

                const classMatchesTag = tagMatch(node.tags, requiredTags);

                // If we have state/issue/tag filters, we MUST filter children
                if (node.children && (requiredStates.length > 0 || requiredIssues.length > 0 || requiredTags.size > 0)) {
                    const matchingChildren = node.children.filter((child: Index) => {
                        const childState = (child.state || "").toLowerCase();
                        const childMatchesState = requiredStates.length === 0 ||
                            requiredStates.some(s => childState.startsWith(s.toLowerCase()));

                        const childMatchesIssueFilter = requiredIssues.length === 0
                            ? true
                            : (classMatchesIssue || matchesAnyIssue(child.issues || [], requiredIssues));

                        const childMatchesTagFilter = requiredTags.size === 0
                            ? true
                            : (classMatchesTag || tagMatch(child.tags, requiredTags));

                        return childMatchesState && childMatchesIssueFilter && childMatchesTagFilter;
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
                    // If no children match the state/issue filter, don't show this node
                    return null;
                }

                // No state/issue/tag filters - use class-level matching
                if (matchesText && matchesPackage && matchesState && classMatchesIssue && classMatchesTag) {
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
    }, [indices, queryMeta, inputValue]);

    const renderedIndices = React.useMemo(
        () => expandProjectChildren(filteredIndices, packageDisplay, packageDisplayRoot),
        [filteredIndices, packageDisplay, packageDisplayRoot],
    );

    const rawGlobalCounts = React.useMemo(() => countStates(indices), [indices]);

    const stateCountsById = React.useMemo(() => {
        const map = new Map<string, StateCounts>();

        const walk = (nodes: Indices) => {
            for (const node of nodes) {
                if (node.id && node.testClass && node.children && node.children.length > 0) {
                    const counts = countChildStates(node);
                    if (counts.failed > 0 || counts.disabled > 0) {
                        map.set(node.id, counts);
                    }
                }

                if (node.children) walk(node.children);
            }
        };

        walk(filteredIndices);
        return map;
    }, [filteredIndices]);

    const prevSearchQueryRef = React.useRef<string | null>(null);
    const prevInputValueRef = React.useRef<string | null>(null);
    const prevFirstMatchingTestRef = React.useRef<Index | null | undefined>(undefined);

    React.useEffect(() => {
        const hasFilter = searchQuery || inputValue;
        const filterChanged = prevSearchQueryRef.current !== searchQuery || prevInputValueRef.current !== inputValue;
        const matchChanged = prevFirstMatchingTestRef.current !== firstMatchingTest;

        if (onFilterApplied && hasFilter && (filterChanged || matchChanged)) {
            onFilterApplied(firstMatchingTest, firstMatchingMethod, matchingMethodsMap);
        }

        prevSearchQueryRef.current = searchQuery;
        prevInputValueRef.current = inputValue;
        prevFirstMatchingTestRef.current = firstMatchingTest;
    }, [firstMatchingTest, firstMatchingMethod, matchingMethodsMap, onFilterApplied, searchQuery, inputValue]);

    return (
        <SourceMetaContext.Provider value={sourceMetaById ?? {}}>
        <TreeExpansionProvider nodes={renderedIndices} searchActive={Boolean(searchQuery || inputValue)}>
            <div className="flex flex-col flex-1 min-h-0">
                <div className="p-3 pt-0 group-data-[collapsible=icon]:hidden">
                    {isNative && (
                        <Select value={environment} onValueChange={onEnvChange}>
                            <SelectTrigger className="h-8 text-[13px] font-medium bg-muted/50 border-transparent hover:bg-muted transition-colors group-data-[collapsible=icon]:hidden mb-2">
                                <Globe className="mr-2 h-3.5 w-3.5 text-blue-500"/>
                                <SelectValue/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="local">Local</SelectItem>
                                <SelectItem value="ci">CI / Jenkins</SelectItem>
                                <SelectItem value="staging">Staging</SelectItem>
                            </SelectContent>
                        </Select>
                    )}

                    <div className="flex flex-col gap-2 group-data-[collapsible=icon]:hidden">
                        <Popover open={showPicker} onOpenChange={(open) => !open && setShowPicker(false)}>
                            <PopoverTrigger asChild>
                                <div className="relative flex flex-wrap items-center gap-1.5 p-1.5 bg-muted/30 hover:bg-muted/50 border rounded-md transition-all focus-within:ring-1 focus-within:ring-blue-500/50">
                                    <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-muted-foreground pointer-events-none"/>

                                    <div className="flex flex-wrap items-center gap-1 pl-7 w-full">
                                        {queryMeta.packages.map(p => (
                                            <Badge key={p} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border-indigo-200 dark:border-indigo-800 max-w-[160px]">
                                                <span className="truncate">pkg:{p}</span>
                                                <span className="cursor-pointer shrink-0" onPointerDown={(e) => { e.stopPropagation(); e.preventDefault(); handleRemoveBadge(`pkg:${p}`); }}>
                                                    <X size={10} className="pointer-events-none" />
                                                </span>
                                            </Badge>
                                        ))}
                                        {queryMeta.states.map(s => (
                                            <Badge key={s} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-blue-500/10 text-blue-600 border-blue-200">
                                                state:{s}
                                                <span className="cursor-pointer" onPointerDown={(e) => { e.stopPropagation(); e.preventDefault(); handleRemoveBadge(`state:${s}`); }}>
                                                        <X size={10} className="pointer-events-none" />
                                                    </span>
                                            </Badge>
                                        ))}
                                        {queryMeta.issues.map(i => (
                                            <Badge key={i} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-amber-500/10 text-amber-600 border-amber-200">
                                                issue:{i}
                                                <span className="cursor-pointer" onPointerDown={(e) => { e.stopPropagation(); e.preventDefault(); handleRemoveBadge(`issue:${i}`); }}>
                                                        <X size={10} className="pointer-events-none" />
                                                    </span>
                                            </Badge>
                                        ))}
                                        {queryMeta.tags.map(t => (
                                            <Badge key={t} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-neutral-500/10 text-neutral-700 dark:text-neutral-300 border-neutral-400/60 max-w-[160px]">
                                                <span className="truncate">tag:{t}</span>
                                                <span className="cursor-pointer shrink-0" onPointerDown={(e) => { e.stopPropagation(); e.preventDefault(); handleRemoveBadge(`tag:${t}`); }}>
                                                    <X size={10} className="pointer-events-none" />
                                                </span>
                                            </Badge>
                                        ))}
                                        {queryMeta.text && (
                                            <Badge variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-muted/80 text-muted-foreground border-border max-w-[160px]">
                                                <span className="truncate">{queryMeta.text}</span>
                                                <span className="cursor-pointer shrink-0" onPointerDown={(e) => { e.stopPropagation(); e.preventDefault(); handleRemoveText(); }}>
                                                    <X size={10} className="pointer-events-none" />
                                                </span>
                                            </Badge>
                                        )}

                                        <input
                                            ref={inputRef}
                                            placeholder="Filter tests..."
                                            className="flex-1 bg-transparent border-none py-0.5 text-[13px] outline-none min-w-[120px]"
                                            value={inputValue}
                                            onChange={(e) => handleInputChange(e.target.value)}
                                            onKeyDown={(e) => {
                                                if (showPicker && pickerItems.length > 0) {
                                                    if (e.key === 'ArrowDown') {
                                                        e.preventDefault();
                                                        setPickerIndex(i => Math.min(i + 1, pickerItems.length - 1));
                                                        return;
                                                    }
                                                    if (e.key === 'ArrowUp') {
                                                        e.preventDefault();
                                                        setPickerIndex(i => Math.max(i - 1, 0));
                                                        return;
                                                    }
                                                    if (e.key === 'Enter') {
                                                        e.preventDefault();
                                                        applySelection(pickerItems[pickerIndex]);
                                                        return;
                                                    }
                                                }

                                                if (e.key === ' ' && showPicker) {
                                                    setShowPicker(false);
                                                }

                                                if (e.key === 'Enter' && !showPicker && inputValue.trim() !== "") {
                                                    const val = inputValue.trim();
                                                    if (val.startsWith('state:') || val.startsWith('issue:') || val.startsWith('tag:')) {
                                                        onSearchChange(`${searchQuery} ${val}`.trim());
                                                        setInputValue("");
                                                    }
                                                }

                                                if (e.key === 'Escape') {
                                                    setInputValue("");
                                                    onSearchChange("");
                                                    setShowPicker(false);
                                                    inputRef?.current?.focus();
                                                }

                                                if (e.key === 'Backspace' && inputValue === '') {
                                                    const parts = searchQuery.trim().split(/\s+/);
                                                    if (parts.length > 0 && parts[0] !== '') {
                                                        onSearchChange(parts.join(' '));
                                                    }
                                                }
                                            }}
                                        />
                                        <KbdGroup className="pointer-events-none absolute right-1.5 top-2 hidden select-none sm:flex">
                                            <Kbd>/</Kbd>
                                            <Kbd>⌘+K</Kbd>
                                        </KbdGroup>
                                    </div>
                                </div>
                            </PopoverTrigger>
                            <PopoverContent
                                className="p-0 w-[200px]"
                                align="start"
                                side="bottom"
                                onOpenAutoFocus={(e) => e.preventDefault()}
                            >
                                <div className="rounded-lg bg-popover p-1">
                                    <div className="px-2 py-1.5 text-xs font-medium text-muted-foreground">
                                        {pickerType === 'state' ? "Select State" : pickerType === 'tag' ? "Select Tag" : "Select Issue"}
                                    </div>
                                    <div ref={pickerListRef} className="max-h-[200px] overflow-y-auto">
                                        {pickerItems.length === 0 ? (
                                            <div className="px-2 py-1.5 text-xs text-muted-foreground">No matches found.</div>
                                        ) : (
                                            pickerItems.map((item, idx) => (
                                                <div
                                                    key={item}
                                                    className={cn(
                                                        "flex items-center rounded-sm px-2 py-1.5 text-[13px] cursor-pointer",
                                                        idx === pickerIndex
                                                            ? "bg-accent text-accent-foreground"
                                                            : "text-popover-foreground hover:bg-accent/50"
                                                    )}
                                                    onPointerDown={(e) => {
                                                        e.preventDefault();
                                                        e.stopPropagation();
                                                        applySelection(item);
                                                    }}
                                                    onPointerEnter={() => setPickerIndex(idx)}
                                                >
                                                    {item}
                                                </div>
                                            ))
                                        )}
                                    </div>
                                </div>
                            </PopoverContent>
                        </Popover>
                    </div>
                </div>

                <SidebarContent className="px-2 gap-1">
                    <SidebarGroup className="p-1">
                        <SidebarGroupLabel className="flex items-center justify-between h-7 px-2 text-[10px] uppercase tracking-widest font-bold text-muted-foreground/90">
                            <span>Test Explorer</span>
                            <div className="group-data-[collapsible=icon]:hidden">
                                <SidebarTreeToolbar/>
                            </div>
                        </SidebarGroupLabel>
                        <SidebarMenu className="gap-0.5">
                            {renderedIndices.map((node) => (
                                <RecursiveMenuItem key={node.id} node={node} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById} testMethodMap={testMethodMap} matchingMethodsMap={matchingMethodsMap}/>
                            ))}
                        </SidebarMenu>
                    </SidebarGroup>
                </SidebarContent>

                <div className="p-3 pt-0 group-data-[collapsible=icon]:hidden">
                    <StateCountBar
                        counts={rawGlobalCounts}
                        activeStates={queryMeta.states}
                        onToggle={(s) => onSearchChange(setStateFilter(searchQuery, s))}
                    />
                </div>
            </div>
        </TreeExpansionProvider>
        </SourceMetaContext.Provider>
    );
}

interface StateBadgesProps {
    counts: StateCounts;
}

function StateBadges({counts}: StateBadgesProps) {
    return (
        <span className="flex items-center gap-1 shrink-0 ml-auto">
            {counts.failed > 0 && (
                <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-failure/10 text-failure text-[9px] font-bold px-1">
                    {counts.failed}
                </span>
            )}
            {counts.disabled > 0 && (
                <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-disabled/10 text-disabled text-[9px] font-bold px-1">
                    {counts.disabled}
                </span>
            )}
        </span>
    );
}

interface StateCountBarProps {
    counts: StateCounts;
    activeStates: readonly string[];
    onToggle: (state: string) => void;
}

const RING_BY_KEY: Record<"passed" | "failed" | "disabled", string> = {
    passed: "ring-success/60",
    failed: "ring-failure/60",
    disabled: "ring-disabled/60",
};

const HOVER_BG_BY_KEY: Record<"passed" | "failed" | "disabled", string> = {
    passed: "hover:bg-success/10",
    failed: "hover:bg-failure/10",
    disabled: "hover:bg-disabled/10",
};

function StateCountBar({counts, activeStates, onToggle}: StateCountBarProps) {
    const {total} = counts;
    if (total === 0) return null;

    const active = BAR_SEGMENTS
        .map(s => ({...s, count: counts[s.key]}))
        .filter(s => s.count > 0);

    const anyActive = activeStates.length > 0;
    const isActive = (key: string) => activeStates.includes(key);

    return (
        <div className="space-y-2">
            <div className="flex h-1.5 rounded-full overflow-hidden bg-muted/50">
                {active.map((seg) => (
                    <div
                        key={seg.key}
                        className={cn(
                            "transition-all duration-500",
                            seg.barCls,
                            anyActive && !isActive(seg.key) && "opacity-30"
                        )}
                        style={{width: `${(seg.count / total) * 100}%`}}
                    />
                ))}
            </div>

            <div className="flex items-center justify-center gap-2">
                {active.map((seg) => {
                    const pressed = isActive(seg.key);
                    return (
                        <button
                            key={seg.key}
                            type="button"
                            aria-pressed={pressed}
                            onClick={() => onToggle(seg.key)}
                            title={pressed ? `Remove ${seg.key} filter` : `Show only ${seg.key} tests`}
                            className={cn(
                                "inline-flex items-center gap-1 rounded-md px-1.5 py-0.5 text-[10px] font-bold cursor-pointer transition-all",
                                seg.color, seg.bg,
                                HOVER_BG_BY_KEY[seg.key],
                                pressed && cn("ring-1", RING_BY_KEY[seg.key]),
                                anyActive && !pressed && "opacity-50"
                            )}
                        >
                            {seg.count} {seg.key}
                        </button>
                    );
                })}
            </div>
        </div>
    );
}

interface CollapsibleMenuNodeProps {
    node: Index;
    onSelect: (node: Index, firstMatchingMethod: string | null, allMatchingMethods: string[]) => void;
    selectedId: string | null;
    stateCountsById: Map<string, StateCounts>;
    iconTone: string;
    childCounts: StateCounts | null;
    labelClassName: string;
    children: Index[];
    testMethodMap: Map<string, string | null>;
    matchingMethodsMap: Map<string, string[]>;
    generatedAt?: string;
}

function CollapsibleMenuNode({node, onSelect, selectedId, stateCountsById, iconTone, childCounts, labelClassName, children, testMethodMap, matchingMethodsMap, generatedAt}: CollapsibleMenuNodeProps) {
    const {isCollapsed, setCollapsed} = useTreeExpansion();
    const open = !isCollapsed(node.id);
    const sortedChildren = React.useMemo(() =>
        [...children].sort((a, b) => {
            const aFolder = a.type !== 'test';
            const bFolder = b.type !== 'test';
            return aFolder === bFolder ? 0 : aFolder ? -1 : 1;
        }),
        [children]
    );

    return (
        <Collapsible open={open} onOpenChange={(o) => setCollapsed(node.id, !o)} className="group/collapsible">
            <SidebarMenuItem>
                <CollapsibleTrigger asChild>
                    <SidebarMenuButton
                        size="sm"
                        className={labelClassName}
                    >
                        <ChevronRight
                            className={cn(
                                "h-3 w-3 transition-transform duration-200 opacity-50",
                                open && "rotate-90"
                            )}
                        />
                        {open
                            ? <FolderOpen className={cn("h-3.5 w-3.5", iconTone)}/>
                            : <Folder className={cn("h-3.5 w-3.5", iconTone)}/>
                        }
                        <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
                        {generatedAt && (
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <span
                                        className="text-[10px] font-mono text-muted-foreground/40 select-none whitespace-nowrap shrink-0 group-data-[collapsible=icon]:hidden"
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        {formatRelative(generatedAt)}
                                    </span>
                                </TooltipTrigger>
                                <TooltipContent side="right" className="text-[11px] font-mono">
                                    {formatGeneratedAt(generatedAt)}
                                </TooltipContent>
                            </Tooltip>
                        )}
                        {childCounts && <StateBadges counts={childCounts}/>}
                    </SidebarMenuButton>
                </CollapsibleTrigger>

                <CollapsibleContent>
                    <SidebarMenuSub className="ml-3 border-l border-border/50 pl-2 py-0">
                        {sortedChildren.map((child) => (
                            <RecursiveMenuItem key={child.id} node={child} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById} testMethodMap={testMethodMap} matchingMethodsMap={matchingMethodsMap}/>
                        ))}
                    </SidebarMenuSub>
                </CollapsibleContent>
            </SidebarMenuItem>
        </Collapsible>
    );
}

interface RecursiveMenuItemProps {
    node: Index;
    onSelect: (node: Index, firstMatchingMethod: string | null, allMatchingMethods: string[]) => void;
    selectedId: string | null;
    stateCountsById: Map<string, StateCounts>;
    testMethodMap: Map<string, string | null>;
    matchingMethodsMap: Map<string, string[]>;
}

const RecursiveMenuItem = React.memo(function RecursiveMenuItem({node, onSelect, selectedId, stateCountsById, testMethodMap, matchingMethodsMap}: RecursiveMenuItemProps) {
    const sourceMetaById = React.useContext(SourceMetaContext);
    const location = useLocation();
    const isSelected = selectedId === node.id;

    const iconTone =
        node.state === "Failed"
            ? "text-failure"
            : node.state === "Passed"
                ? "text-success"
                : node.state === "Disabled" && node.type === "test"
                    ? "text-disabled opacity-70"
                    : "text-muted-foreground/70";

    const childCounts = node.id ? stateCountsById.get(node.id) ?? null : null;

    if (node.type === 'project') {
        const generatedAt = node.sourceId ? sourceMetaById[node.sourceId]?.generatedAt : undefined;

        return (
            <CollapsibleMenuNode
                node={node} onSelect={onSelect} selectedId={selectedId}
                stateCountsById={stateCountsById}
                iconTone={iconTone} childCounts={childCounts}
                labelClassName="text-[13px] font-bold text-foreground"
                children={node.children || []}
                testMethodMap={testMethodMap}
                matchingMethodsMap={matchingMethodsMap}
                generatedAt={generatedAt}
            />
        );
    }

    if (node.type === 'package') {
        return (
            <CollapsibleMenuNode
                node={node} onSelect={onSelect} selectedId={selectedId}
                stateCountsById={stateCountsById}
                iconTone={iconTone} childCounts={childCounts}
                labelClassName="text-[13px] text-muted-foreground hover:text-foreground"
                children={node.children || []}
                testMethodMap={testMethodMap}
                matchingMethodsMap={matchingMethodsMap}
            />
        );
    }

    if (node.type === 'system-view') {
        const isActive = location.pathname === '/system-view'
            && new URLSearchParams(location.search).get('source') === node.sourceId;
        return (
            <SidebarMenuItem>
                <SidebarMenuButton
                    size="sm"
                    isActive={isActive}
                    onClick={() => onSelect(node, null, [])}
                    className={cn(
                        "text-[13px] transition-all",
                        isActive ? "bg-accent text-accent-foreground font-semibold" : "text-muted-foreground"
                    )}
                >
                    <Network className="h-3.5 w-3.5 shrink-0"/>
                    <span>{node.displayName}</span>
                </SidebarMenuButton>
            </SidebarMenuItem>
        );
    }

    return (
        <SidebarMenuItem>
            <SidebarMenuButton
                size="sm"
                isActive={isSelected}
                onClick={() => {
                    const method = testMethodMap.get(node.id) ?? null;
                    const allMethods = matchingMethodsMap.get(node.id) ?? [];
                    onSelect(node, method, allMethods);
                }}
                className={cn(
                    "text-[13px] transition-all pl-6",
                    isSelected ? "bg-accent text-accent-foreground font-semibold" : "text-muted-foreground",
                    node.state === 'Failed' && !isSelected && "text-failure font-medium",
                    node.state === 'Disabled' && !isSelected && "text-disabled opacity-70"
                )}
            >
                <Diamond strokeWidth={2.5} className={cn(
                    "h-3 w-3 shrink-0",
                    isSelected ? "fill-current" : "",
                    iconTone)}/>
                <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
                {node.hasErrors && (
                    <AlertTriangle className="h-[14px] w-[14px] shrink-0 text-amber-500 dark:text-amber-400" aria-label="Has parse/render errors" />
                )}
                {childCounts && <StateBadges counts={childCounts}/>}
            </SidebarMenuButton>
        </SidebarMenuItem>
    );
});

function formatGeneratedAt(iso: string): string {
    try {
        return new Intl.DateTimeFormat(undefined, {
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: false
        }).format(new Date(iso));
    } catch {
        return iso;
    }
}

function formatRelative(iso: string): string {
    try {
        const t = new Date(iso).getTime();
        if (Number.isNaN(t)) return iso;
        const seconds = Math.max(0, Math.round((Date.now() - t) / 1000));
        if (seconds < 60) return 'just now';
        const minutes = Math.round(seconds / 60);
        if (minutes < 60) return `${minutes}m ago`;
        const hours = Math.round(minutes / 60);
        if (hours < 24) return `${hours}h ago`;
        const days = Math.round(hours / 24);
        if (days < 2) return 'yesterday';
        if (days < 7) return `${days}d ago`;
        const date = new Date(iso);
        const sameYear = date.getFullYear() === new Date().getFullYear();
        return new Intl.DateTimeFormat(undefined, sameYear
            ? {day: '2-digit', month: 'short'}
            : {day: '2-digit', month: 'short', year: 'numeric'}
        ).format(date);
    } catch {
        return iso;
    }
}
