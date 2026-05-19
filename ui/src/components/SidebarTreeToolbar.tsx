import {useEffect} from "react"
import {ChevronsDown, ChevronsUp} from "lucide-react"
import {Tooltip, TooltipContent, TooltipTrigger} from "@/components/ui/tooltip"
import {useTreeExpansion} from "@/hooks/useTreeExpansion"

const ICON_BTN =
    "inline-flex items-center justify-center h-5 w-5 rounded text-muted-foreground/70 hover:text-foreground hover:bg-accent/60 transition-colors";

const TIP = "text-[11px] leading-none px-2 py-1 normal-case tracking-normal font-normal flex items-center gap-2";

function Shortcut({keys}: {keys: string}) {
    return <kbd className="font-sans opacity-60 text-[10px] tracking-tight">{keys}</kbd>;
}

export function SidebarTreeToolbar() {
    const {expandAll, collapseAll} = useTreeExpansion();

    useEffect(() => {
        const handler = (e: KeyboardEvent) => {
            if (!e.altKey || e.shiftKey || e.metaKey || e.ctrlKey) return;
            const target = e.target as HTMLElement | null;
            const tag = target?.tagName;
            const editable = tag === "INPUT" || tag === "TEXTAREA" || target?.isContentEditable;
            if (editable) return;

            if (e.code === "BracketRight") {
                e.preventDefault();
                expandAll();
            } else if (e.code === "BracketLeft") {
                e.preventDefault();
                collapseAll();
            }
        };
        window.addEventListener("keydown", handler, true);
        return () => window.removeEventListener("keydown", handler, true);
    }, [expandAll, collapseAll]);

    return (
        <div className="flex items-center gap-0.5 -mr-1">
            <Tooltip>
                <TooltipTrigger asChild>
                    <button
                        type="button"
                        className={ICON_BTN}
                        onClick={() => expandAll()}
                        aria-label="Expand all folders"
                    >
                        <ChevronsDown size={12}/>
                    </button>
                </TooltipTrigger>
                <TooltipContent side="bottom" className={TIP}>
                    Expand all <Shortcut keys="⌥]"/>
                </TooltipContent>
            </Tooltip>

            <Tooltip>
                <TooltipTrigger asChild>
                    <button
                        type="button"
                        className={ICON_BTN}
                        onClick={() => collapseAll()}
                        aria-label="Collapse all folders"
                    >
                        <ChevronsUp size={12}/>
                    </button>
                </TooltipTrigger>
                <TooltipContent side="bottom" className={TIP}>
                    Collapse all <Shortcut keys="⌥["/>
                </TooltipContent>
            </Tooltip>
        </div>
    );
}
