import * as React from "react"
import GithubIcon from "@/assets/github-mark.svg?react"
import KensaLogo from "@/assets/logo.svg?react"
import {Search, Package, FileText, Globe} from "lucide-react"
import {buildTree, TreeNode} from "@/utils/treeUtils"
import {cn} from "@/lib/utils"
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
import {Indices} from "@/types/Index";

interface AppSidebarProps {
    indices: Indices;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onSelect: (node: TreeNode) => void;
    selectedId: string | null;
    environment: string;
    onEnvChange: (env: string) => void;
    isNative?: boolean;
}

export function AppSidebar({indices, searchQuery, onSearchChange, onSelect, selectedId, environment, onEnvChange, isNative}: AppSidebarProps) {
    const tree = React.useMemo(() => {
        const filtered = indices.filter((idx) =>
            idx.displayName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            idx.testClass.toLowerCase().includes(searchQuery.toLowerCase())
        );
        return buildTree(filtered);
    }, [indices, searchQuery]);

    return (
        <Sidebar className="w-full border-none">
            <SidebarHeader className="p-3 pb-0 gap-3">
                <div className="flex items-center justify-between px-1">
                    <div className="flex items-center gap-2">
                        <KensaLogo className="w-6 h-6 text-emerald-500 drop-shadow-sm"/>
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
                        <SelectTrigger className="h-8 text-[11px] font-medium bg-muted/50 border-transparent hover:bg-muted transition-colors group-data-[collapsible=icon]:hidden">
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

                <div className="relative group-data-[collapsible=icon]:hidden">
                    <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-muted-foreground"/>
                    <input
                        placeholder="Filter tests..."
                        className="w-full bg-muted/30 hover:bg-muted/50 focus:bg-background border-none rounded-md py-2 pl-8 pr-3 text-[11px] transition-all outline-none ring-1 ring-border/50 focus:ring-blue-500/50"
                        value={searchQuery}
                        onChange={(e) => onSearchChange(e.target.value)}
                    />
                </div>
            </SidebarHeader>

            <SidebarContent className="px-2">
                <SidebarGroup>
                    <SidebarGroupLabel className="text-[10px] uppercase tracking-widest font-bold text-muted-foreground/70">Test Explorer</SidebarGroupLabel>
                    <SidebarMenu className="gap-0.5">
                        {tree.map((node) => (
                            <RecursiveMenuItem key={node.id} node={node} onSelect={onSelect} selectedId={selectedId}/>
                        ))}
                    </SidebarMenu>
                </SidebarGroup>
            </SidebarContent>
        </Sidebar>
    )
}

function RecursiveMenuItem({node, onSelect, selectedId}: { node: TreeNode; onSelect: (node: TreeNode) => void; selectedId: string | null }) {
    const isSelected = selectedId === node.id;

    if (node.type === 'package') {
        return (
            <SidebarMenuItem>
                <SidebarMenuButton className="h-8 text-[12px] text-slate-500 hover:text-slate-900 dark:hover:text-slate-200">
                    <Package className="h-3.5 w-3.5 opacity-70"/>
                    <span>{node.name}</span>
                </SidebarMenuButton>
                {node.children.length > 0 && (
                    <SidebarMenuSub className="ml-3 border-l border-border/50 pl-2">
                        {node.children.map((child) => (
                            <RecursiveMenuItem key={child.id} node={child} onSelect={onSelect} selectedId={selectedId}/>
                        ))}
                    </SidebarMenuSub>
                )}
            </SidebarMenuItem>
        );
    }

    return (
        <SidebarMenuItem>
            <SidebarMenuButton
                isActive={isSelected}
                onClick={() => onSelect(node)}
                className={cn(
                    "h-8 text-[12px] transition-all",
                    isSelected ? "bg-accent text-accent-foreground font-semibold" : "text-muted-foreground",
                    node.state === 'Failed' && !isSelected && "text-destructive font-medium"
                )}
            >
                <FileText className={cn("h-3.5 w-3.5", node.state === 'Failed' ? "text-destructive" : "opacity-50")}/>
                <span className="truncate">{node.name}</span>
                {node.state === 'Failed' && (
                    <div className="ml-auto">
                        <div className="w-1.5 h-1.5 rounded-full bg-destructive shadow-[0_0_8px_rgba(244,63,94,0.4)]"/>
                    </div>
                )}
            </SidebarMenuButton>
        </SidebarMenuItem>
    );
}