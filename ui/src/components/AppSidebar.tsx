import * as React from "react"
import GithubIcon from "@/assets/github-mark.svg?react"
import KensaLogo from "@/assets/logo.svg?react"
import {Sidebar, SidebarFooter, SidebarHeader,} from "@/components/ui/sidebar"
import {Index, Indices} from "@/types/Index";
import {useConfig} from "@/contexts/ConfigContext"
import {TestExplorer} from "@/components/TestExplorer"

interface AppSidebarProps {
    indices: Indices;
    sourceMetaById?: Record<string, { generatedAt?: string }>;
    searchQuery: string;
    onSearchChange: (query: string) => void;
    onSelect: (node: Index, firstMatchingMethod: string | null, allMatchingMethods: string[]) => void;
    selectedId: string | null;
    selectedResultKey?: string | null;
    environment: string;
    onEnvChange: (env: string) => void;
    isNative?: boolean;
    inputRef?: React.RefObject<HTMLInputElement>;
    onFilterApplied?: (firstTest: Index | null, firstMethod: string | null, matchingMethodsMap: Map<string, string[]>) => void;
}

export function AppSidebar(props: AppSidebarProps) {
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
            </SidebarHeader>

            <TestExplorer {...props}/>

            <SidebarFooter className="p-3 pt-0">
                <ReportMeta/>
            </SidebarFooter>
        </Sidebar>
    );
}

function ReportMeta() {
    const {kensaVersion} = useConfig();
    if (!kensaVersion) return null;

    return (
        <div className="flex items-center justify-center gap-1.5 pt-1.5 border-t border-border/30 group-data-[collapsible=icon]:hidden">
            <span className="text-[9px] font-mono text-muted-foreground/55 select-none tracking-wide">
                v{kensaVersion}
            </span>
        </div>
    );
}
