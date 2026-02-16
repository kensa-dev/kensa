import * as React from "react"
import GithubIcon from "@/assets/github-mark.svg?react"
import KensaLogo from "@/assets/logo.svg?react"
import {Search, Globe, ChevronRight, X} from "lucide-react"
import { Folder, FolderOpen, Diamond } from "lucide-react"
import {buildTree} from "@/utils/treeUtils"
import {cn} from "@/lib/utils"
import {Badge} from "@/components/ui/badge"
import {
    Sidebar,
    SidebarContent,
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

    const states = ['passed', 'failed', 'disabled'];

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
                            <RecursiveMenuItem key={node.id} node={node} onSelect={onSelect} selectedId={selectedId}/>
                        ))}
                    </SidebarMenu>
                </SidebarGroup>
            </SidebarContent>
        </Sidebar>
    );
}

interface RecursiveMenuItemProps {
    node: Index;
    onSelect: (node: Index) => void;
    selectedId: string | null;
}

function RecursiveMenuItem({node, onSelect, selectedId}: RecursiveMenuItemProps) {
    const isSelected = selectedId === node.id;

    const iconTone =
        node.state === "Failed"
            ? "text-destructive"
            : node.state === "Passed"
                ? "text-success dark:text-success"
                : "text-muted-foreground/90";

    if (node.type === 'project') {
        const treeChildren = buildTree(node.children || []);
        const [open, setOpen] = React.useState(true);

        return (
            <Collapsible open={open} onOpenChange={setOpen} className="group/collapsible">
                <SidebarMenuItem>
                    <CollapsibleTrigger asChild>
                        <SidebarMenuButton size="sm" className="text-[12px] font-bold text-foreground">
                            <ChevronRight
                                className={cn(
                                    "h-3 w-3 transition-transform duration-200 opacity-50",
                                    open && "rotate-90"
                                )}
                            />
                            {open
                                ? <FolderOpen className={cn("h-3.5 w-3.5", iconTone)} />
                                : <Folder className={cn("h-3.5 w-3.5", iconTone)} />
                            }
                            <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
                        </SidebarMenuButton>
                    </CollapsibleTrigger>

                    <CollapsibleContent>
                        <SidebarMenuSub className="ml-3 border-l border-border/50 pl-2 py-0">
                            {treeChildren.map((child) => (
                                <RecursiveMenuItem key={child.id} node={child} onSelect={onSelect} selectedId={selectedId}/>
                            ))}
                        </SidebarMenuSub>
                    </CollapsibleContent>
                </SidebarMenuItem>
            </Collapsible>
        );
    }

    if (node.type === 'package') {
        const [open, setOpen] = React.useState(true);

        return (
            <Collapsible open={open} onOpenChange={setOpen} className="group/collapsible">
                <SidebarMenuItem>
                    <CollapsibleTrigger asChild>
                        <SidebarMenuButton size="sm" className="text-[12px] text-slate-500 hover:text-slate-900 dark:hover:text-slate-200">
                            <ChevronRight
                                className={cn(
                                    "h-3 w-3 transition-transform duration-200 opacity-50",
                                    open && "rotate-90"
                                )}
                            />
                            {open
                                ? <FolderOpen className={cn("h-3.5 w-3.5", iconTone)} />
                                : <Folder className={cn("h-3.5 w-3.5", iconTone)} />
                            }
                            <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
                        </SidebarMenuButton>
                    </CollapsibleTrigger>

                    <CollapsibleContent>
                        <SidebarMenuSub className="ml-3 border-l border-border/50 pl-2 py-0">
                            {node.children?.map((child) => (
                                <RecursiveMenuItem key={child.id} node={child} onSelect={onSelect} selectedId={selectedId}/>
                            ))}
                        </SidebarMenuSub>
                    </CollapsibleContent>
                </SidebarMenuItem>
            </Collapsible>
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
                    node.state === 'Failed' && !isSelected && "text-destructive font-medium"
                )}
            >
                <Diamond className={cn("h-3.5 w-3.5", iconTone)}/>
                <span className="sidebar-label flex-1 min-w-0 truncate">{node.displayName}</span>
            </SidebarMenuButton>
        </SidebarMenuItem>
    );
}