import * as React from "react"
import {Activity, X} from "lucide-react"
import {Dialog, DialogContent, DialogHeader, DialogTitle} from "@/components/ui/dialog"
import {InteractionContent} from "./InteractionContent"
import {cn} from "@/lib/utils"

export function InteractionDialog({interaction, isOpen, onClose, isPassed}: any) {
    const [isMaximized, setIsMaximized] = React.useState(false)

    const hasValidPayload = React.useMemo(() => {
        if (!interaction?.rendered?.values) return false;
        return interaction.rendered.values.some((p: any) => p.value && p.value.trim() !== "");
    }, [interaction]);

    React.useEffect(() => {
        if (!isOpen) setIsMaximized(false)
    }, [isOpen])

    if (!interaction) return null

    return (
        <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
            <DialogContent
                onEscapeKeyDown={(e) => {
                    const activeElement = document.activeElement;
                    if (activeElement instanceof HTMLInputElement && activeElement.value !== "") {
                        e.preventDefault();
                        return;
                    }
                    if (isMaximized) {
                        e.preventDefault();
                        setIsMaximized(false);
                        return;
                    }
                }}
                className={cn(
                    "fixed flex flex-col gap-0 p-0 overflow-hidden transition-all duration-500 ease-in-out outline-none shadow-2xl [&>button]:hidden border-none bg-background max-w-none",

                    "left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2",

                    (isMaximized && hasValidPayload)
                        ? "w-[calc(100vw-2rem)] h-[calc(100vh-2rem)] rounded-2xl ring-1 ring-border/50 shadow-none"
                        : "w-[90vw] lg:w-[1024px] h-[80vh] rounded-xl border border-border shadow-2xl"
                )}
            >
                {!isMaximized && (
                    <DialogHeader className={cn(
                        "px-6 h-12 flex flex-row items-center justify-between border-b shrink-0 transition-colors animate-in fade-in duration-500",
                        isPassed ? "bg-emerald-500/10 border-emerald-500/20" : "bg-rose-500/10 border-rose-500/20"
                    )}>
                        <div className="flex items-center gap-3 overflow-hidden mr-auto">
                            <Activity className={isPassed ? "text-emerald-600" : "text-rose-600"} size={18}/>
                            <DialogTitle className="text-xs font-bold truncate uppercase tracking-widest opacity-80 text-foreground">
                                {interaction.name}
                            </DialogTitle>
                        </div>

                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-destructive/10 hover:text-destructive rounded-md text-muted-foreground transition-colors focus:outline-none"
                        >
                            <X size={18}/>
                        </button>
                    </DialogHeader>
                )}

                <div className={cn(
                    "flex-1 overflow-hidden transition-all duration-500",
                    isMaximized ? "p-0" : "p-4 bg-muted/10"
                )}>
                    <InteractionContent
                        interaction={interaction}
                        isPassed={isPassed}
                        hideMetadata={isMaximized && hasValidPayload}
                        isMaximized={isMaximized}
                        onToggleMaximize={() => setIsMaximized(!isMaximized)}
                    />
                </div>
            </DialogContent>
        </Dialog>
    )
}