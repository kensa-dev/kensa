import * as React from "react"
import GithubIcon from "@/assets/github-mark.svg?react"
import KensaLogo from "@/assets/logo.svg?react"
import {Search, Globe, ChevronRight, X} from "lucide-react"
import {Folder, FolderOpen, Diamond} from "lucide-react"
import {buildTree} from "@/utils/treeUtils"
import {cn} from "@/lib/utils"
import {Badge} from "@/components/ui/badge"
import {
    Sidebar,
    SidebarContent, SidebarFooter,
    SidebarGroup,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarMenuSub,
} from "@/components/ui/sidebar"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {Index, Indices} from "@/types/Index";
import {
    Collapsible,
    CollapsibleContent,
    CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"

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
    {key: "passed" as const, color: "text-success", bg: "bg-success-10", barCls: "bg-[hsl(var(--success))]"},
    {key: "failed" as const, color: "text-failure", bg: "bg-failure-10", barCls: "bg-[hsl(var(--failure))]"},
    {key: "disabled" as const, color: "text-disabled", bg: "bg-disabled-10", barCls: "bg-[hsl(var(--disabled))]"},
] as const;

interface AppSidebarProps {
    indices: Indices;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onSelect: (node: Index) => void;
    selectedId: string | null;
    environment: string;
    onEnvChange: (env: string) => void;
    isNative?: boolean;
    inputRef?: React.RefObject<HTMLInputElement>;
}

export function AppSidebar({indices, searchQuery, onSearchChange, onSelect, selectedId, environment, onEnvChange, isNative, inputRef}: AppSidebarProps) {
    const [inputValue, setInputValue] = React.useState("");
    const [showPicker, setShowPicker] = React.useState(false);
    const [pickerType, setPickerType] = React.useState<'state' | 'issue' | null>(null);

    const handleRemoveBadge = (token: string) => {
        const newQuery = searchQuery
            .split(/\s+/)
            .filter(part => part !== token)
            .join(' ')
            .trim();
        onSearchChange(newQuery);
    };

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

    const states = FILTER_STATES;

    const handleInputChange = (val: string) => {
        setInputValue(val);
        if (val.endsWith('state:')) {
            setPickerType('state');
            setShowPicker(true);
        } else if (val.endsWith('issue:')) {
            setPickerType('issue');
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
        const parts = searchQuery.split(/\s+/);
        const issues = parts.filter(p => p.startsWith('issue:') && p.length > 6).map(p => p.split(':')[1]);
        const states = parts.filter(p => p.startsWith('state:') && p.length > 6).map(p => p.split(':')[1].toLowerCase());

        const text = parts.filter(p =>
            !(p.startsWith('issue:') && p.length > 6) &&
            !(p.startsWith('state:') && p.length > 6)
        ).join(' ');

        return {text, issues, states};
    }, [searchQuery]);

    const filteredIndices = React.useMemo(() => {
        const {states, issues} = queryMeta;

        const activeTyping = inputValue.toLowerCase();
        const typingState = activeTyping.startsWith('state:') ? activeTyping.split(':')[1] : null;
        const typingIssue = activeTyping.startsWith('issue:') ? activeTyping.split(':')[1] : null;
        const typingText = (!typingState && !typingIssue) ? activeTyping : "";

        const requiredStates = typingState ? [...states, typingState] : states;
        const requiredIssues = typingIssue ? [...issues, typingIssue] : issues;

        const filterNode = (node: Index): Index | null => {
            const isLeaf = node.testClass && (!node.children || node.children.every((c: Index) => c.testMethod));

            if (isLeaf) {
                const nodeName = (node.displayName || "").toLowerCase();
                const nodeClass = (node.testClass || "").toLowerCase();
                const nodeState = (node.state || "").toLowerCase();
                const nodeIssues = (node.issues || []).map((i: string) => i.toLowerCase());

                const matchesText = !typingText || nodeName.includes(typingText) || nodeClass.includes(typingText);

                const matchesState = requiredStates.length === 0 ||
                    requiredStates.some(s => nodeState.startsWith(s.toLowerCase()));

                const matchesIssue = requiredIssues.length === 0 ||
                    nodeIssues.some(ni => requiredIssues.some(ri => ni.includes(ri.toLowerCase())));

                return (matchesText && matchesState && matchesIssue) ? node : null;
            }

            if (node.children) {
                const filteredChildren: Indices = node.children
                    .map((child: Index) => filterNode(child))
                    .filter((child): child is Index => Boolean(child));

                if (filteredChildren.length > 0) {
                    return {...node, children: filteredChildren};
                }
            }

            return null;
        };

        return indices
            .map(idx => filterNode(idx))
            .filter((idx): idx is Index => Boolean(idx));
    }, [indices, queryMeta, inputValue]);

    const {globalCounts, stateCountsById} = React.useMemo(() => {
        const global: StateCounts = {passed: 0, failed: 0, disabled: 0, total: 0};
        const map = new Map<string, StateCounts>();

        const walk = (nodes: Indices) => {
            for (const node of nodes) {
                if (node.testMethod) {
                    global.total++;
                    if (node.state === "Passed") global.passed++;
                    else if (node.state === "Failed") global.failed++;
                    else if (node.state === "Disabled") global.disabled++;
                }

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
        return {globalCounts: global, stateCountsById: map};
    }, [filteredIndices]);

    return (
        <Sidebar className="w-full border-none">
            <SidebarHeader className="p-3 pb-0 gap-3">
                <div className="flex items-center justify-between px-1">
                    <div className="flex items-center gap-2">
                        <KensaLogo className="w-6 h-6 text-success drop-shadow-sm"/>
                        <span className="group-data-[collapsible=icon]:hidden font-black tracking-tighter text-base text-foreground/90">KENSA</span>
                    </div>
                    <a
                        href="https://github.com/kensa-dev/kensa"
                        target="_blank"
                        className="text-muted-foreground hover:text-foreground transition-colors"
                    >
                        <GithubIcon className="w-4 h-4"/>
                    </a>
                </div>

                {isNative && (
                    <Select value={environment} onValueChange={onEnvChange}>
                        <SelectTrigger className="h-8 text-[12px] font-medium bg-muted/50 border-transparent hover:bg-muted transition-colors group-data-[collapsible=icon]:hidden">
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
                                    {queryMeta.states.map(s => (
                                        <Badge key={s} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-blue-500/10 text-blue-600 border-blue-200">
                                            state:{s} <X size={10} className="cursor-pointer" onClick={() => handleRemoveBadge(`state:${s}`)}/>
                                        </Badge>
                                    ))}
                                    {queryMeta.issues.map(i => (
                                        <Badge key={i} variant="secondary" className="h-5 text-[9px] gap-1 px-1 bg-amber-500/10 text-amber-600 border-amber-200">
                                            issue:{i} <X size={10} className="cursor-pointer" onClick={() => handleRemoveBadge(`issue:${i}`)}/>
                                        </Badge>
                                    ))}

                                    <input
                                        ref={inputRef}
                                        placeholder="Filter tests..."
                                        className="flex-1 bg-transparent border-none py-0.5 text-[12px] outline-none min-w-[120px]"
                                        value={inputValue}
                                        onChange={(e) => handleInputChange(e.target.value)}
                                        onKeyDown={(e) => {
                                            if (e.key === ' ' && showPicker) {
                                                setShowPicker(false);
                                            }

                                            if (e.key === 'Enter' && !showPicker && inputValue.trim() !== "") {
                                                const val = inputValue.trim();
                                                if (val.startsWith('state:') || val.startsWith('issue:')) {
                                                    onSearchChange(`${searchQuery} ${val}`.trim());
                                                    setInputValue("");
                                                }
                                            }

                                            if (e.key === 'Escape') {
                                                setInputValue("");
                                                onSearchChange("");
                                                setShowPicker(false);
                                                e.currentTarget.blur();
                                            }

                                            if (e.key === 'Backspace' && inputValue === '') {
                                                const parts = searchQuery.trim().split(/\s+/);
                                                if (parts.length > 0 && parts[0] !== '') {
                                                    onSearchChange(parts.join(' '));
                                                }
                                            }
                                        }}
                                    />
                                    <kbd className="pointer-events-none absolute right-1.5 top-2 hidden h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium opacity-100 sm:flex">
                                        <span className="text-xs">⌘</span>K
                                    </kbd>
                                </div>
                            </div>
                        </PopoverTrigger>
                        <PopoverContent
                            className="p-0 w-[200px]"
                            align="start"
                            side="bottom"
                            onOpenAutoFocus={(e) => e.preventDefault()}
                        >
                            <Command className="rounded-lg border shadow-md">
                                <CommandList>
                                    <CommandEmpty>No matches found.</CommandEmpty>
                                    <CommandGroup heading={pickerType === 'state' ? "Select State" : "Select Issue"}>
                                        {(pickerType === 'state' ? states : allIssues).map((item) => (
                                            <CommandItem
                                                key={item}
                                                value={item}
                                                onSelect={() => applySelection(item)}
                                                className="text-[12px] cursor-pointer"
                                            >
                                                {item}
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        </PopoverContent>
                    </Popover>
                </div>
            </SidebarHeader>

            <SidebarContent className="px-2 gap-1">
                <SidebarGroup className="p-1">
                    <SidebarGroupLabel className="text-[10px] uppercase tracking-widest font-bold text-muted-foreground/90 h-7 px-2">
                        Test Explorer
                    </SidebarGroupLabel>
                    <SidebarMenu className="gap-0.5">
                        {filteredIndices.map((node) => (
                            <RecursiveMenuItem key={node.id} node={node} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById}/>
                        ))}
                    </SidebarMenu>
                </SidebarGroup>
            </SidebarContent>

            <SidebarFooter className="p-3 pt-0">
                <StateCountBar counts={globalCounts}/>
            </SidebarFooter>
        </Sidebar>
    );
}

function StateBadges({counts}: { counts: StateCounts }) {
    return (
        <span className="flex items-center gap-1 shrink-0 ml-auto">
            {counts.failed > 0 && (
                <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-failure-10 text-failure text-[9px] font-bold px-1">
                    {counts.failed}
                </span>
            )}
            {counts.disabled > 0 && (
                <span className="inline-flex items-center justify-center min-w-[18px] h-[18px] rounded-full bg-disabled-10 text-disabled text-[9px] font-bold px-1">
                    {counts.disabled}
                </span>
            )}
        </span>
    );
}

function StateCountBar({counts}: { counts: StateCounts }) {
    const {total} = counts;
    if (total === 0) return null;

    const active = BAR_SEGMENTS
        .map(s => ({...s, count: counts[s.key]}))
        .filter(s => s.count > 0);

    return (
        <div className="space-y-2">
            <div className="flex h-1.5 rounded-full overflow-hidden bg-muted/50">
                {active.map((seg) => (
                    <div
                        key={seg.key}
                        className={cn("transition-all duration-500", seg.barCls)}
                        style={{width: `${(seg.count / total) * 100}%`}}
                    />
                ))}
            </div>

            <div className="flex items-center justify-center gap-2">
                {active.map((seg) => (
                    <span
                        key={seg.key}
                        className={cn(
                            "inline-flex items-center gap-1 rounded-md px-1.5 py-0.5 text-[10px] font-bold",
                            seg.color, seg.bg
                        )}
                    >
                        {seg.count} {seg.key}
                    </span>
                ))}
            </div>
        </div>
    );
}

interface CollapsibleMenuNodeProps {
    node: Index;
    onSelect: (node: Index) => void;
    selectedId: string | null;
    stateCountsById: Map<string, StateCounts>;
    iconTone: string;
    childCounts: StateCounts | null;
    labelClassName: string;
    children: Index[];
}

function CollapsibleMenuNode({node, onSelect, selectedId, stateCountsById, iconTone, childCounts, labelClassName, children}: CollapsibleMenuNodeProps) {
    const [open, setOpen] = React.useState(true);

    return (
        <Collapsible open={open} onOpenChange={setOpen} className="group/collapsible">
            <SidebarMenuItem>
                <CollapsibleTrigger asChild>
                    <SidebarMenuButton size="sm" className={labelClassName}>
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
                        {childCounts && <StateBadges counts={childCounts}/>}
                    </SidebarMenuButton>
                </CollapsibleTrigger>

                <CollapsibleContent>
                    <SidebarMenuSub className="ml-3 border-l border-border/50 pl-2 py-0">
                        {children.map((child) => (
                            <RecursiveMenuItem key={child.id} node={child} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById}/>
                        ))}
                    </SidebarMenuSub>
                </CollapsibleContent>
            </SidebarMenuItem>
        </Collapsible>
    );
}

interface RecursiveMenuItemProps {
    node: Index;
    onSelect: (node: Index) => void;
    selectedId: string | null;
    stateCountsById: Map<string, StateCounts>;
}

const RecursiveMenuItem = React.memo(function RecursiveMenuItem({node, onSelect, selectedId, stateCountsById}: RecursiveMenuItemProps) {
    const isSelected = selectedId === node.id;

    const iconTone =
        node.state === "Failed"
            ? "text-destructive"
            : node.state === "Passed"
                ? "text-success dark:text-success"
                : "text-muted-foreground/90";

    const childCounts = node.id ? stateCountsById.get(node.id) ?? null : null;

    if (node.type === 'project') {
        return (
            <CollapsibleMenuNode
                node={node} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById}
                iconTone={iconTone} childCounts={childCounts}
                labelClassName="text-[12px] font-bold text-foreground"
                children={buildTree(node.children || [])}
            />
        );
    }

    if (node.type === 'package') {
        return (
            <CollapsibleMenuNode
                node={node} onSelect={onSelect} selectedId={selectedId} stateCountsById={stateCountsById}
                iconTone={iconTone} childCounts={childCounts}
                labelClassName="text-[12px] text-slate-500 hover:text-slate-900 dark:hover:text-slate-200"
                children={node.children || []}
            />
        );
    }

    return (
        <SidebarMenuItem>
            <SidebarMenuButton
                size="sm"
                isActive={isSelected}
                onClick={() => onSelect(node)}
                className={cn(
                    "text-[12px] transition-all",
                    isSelected ? "bg-accent text-accent-foreground font-semibold" : "text-muted-foreground",
                    node.state === 'Failed' && !isSelected && "text-destructive font-medium",
                    node.state === 'Disabled' && !isSelected && "text-disabled opacity-70"
                )}
            >
                <Diamond className={cn("h-3.5 w-3.5 shrink-0", iconTone)}/>
                <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
                {childCounts && <StateBadges counts={childCounts}/>}
            </SidebarMenuButton>
        </SidebarMenuItem>
    );
});