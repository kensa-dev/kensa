import * as PopoverPrimitive from '@radix-ui/react-popover';
import { Search, Tag } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface ContextMenuPosition {
    x: number;
    y: number;
}

interface SuiteSearchContextMenuProps {
    open: boolean;
    position: ContextMenuPosition | null;
    fixtureNames: string[];
    onFindValue: () => void;
    onFindFixture: (name: string) => void;
    onOpenChange: (open: boolean) => void;
}

export const SuiteSearchContextMenu = ({
    open,
    position,
    fixtureNames,
    onFindValue,
    onFindFixture,
    onOpenChange,
}: SuiteSearchContextMenuProps) => {
    return (
        <PopoverPrimitive.Root open={open} onOpenChange={onOpenChange}>
            <PopoverPrimitive.Anchor
                style={{
                    position: 'fixed',
                    left: position?.x ?? 0,
                    top: position?.y ?? 0,
                    width: 0,
                    height: 0,
                }}
            />
            <PopoverPrimitive.Portal>
                <PopoverPrimitive.Content
                    side="bottom"
                    align="start"
                    sideOffset={4}
                    onOpenAutoFocus={(e) => e.preventDefault()}
                    className={cn(
                        'z-50 min-w-56 rounded-md border bg-popover p-1 text-popover-foreground shadow-md outline-none',
                        'data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
                    )}
                >
                    <button
                        type="button"
                        onClick={onFindValue}
                        className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-left text-sm hover:bg-accent hover:text-accent-foreground"
                    >
                        <Search className="h-3.5 w-3.5 shrink-0 opacity-70" />
                        <span className="truncate">
                            Find this value across suite
                        </span>
                    </button>

                    {fixtureNames.length === 0 ? null : fixtureNames.length === 1 ? (
                        <button
                            type="button"
                            onClick={() => onFindFixture(fixtureNames[0])}
                            className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-left text-sm hover:bg-accent hover:text-accent-foreground"
                        >
                            <Tag className="h-3.5 w-3.5 shrink-0 opacity-70" />
                            <span className="truncate">
                                Find this fixture across suite
                                <span className="ml-1 font-mono text-xs opacity-60">({fixtureNames[0]})</span>
                            </span>
                        </button>
                    ) : (
                        fixtureNames.map((name) => (
                            <button
                                key={name}
                                type="button"
                                onClick={() => onFindFixture(name)}
                                className="flex w-full items-center gap-2 rounded-sm px-2 py-1.5 text-left text-sm hover:bg-accent hover:text-accent-foreground"
                            >
                                <Tag className="h-3.5 w-3.5 shrink-0 opacity-70" />
                                <span className="truncate">
                                    Find fixture
                                    <span className="ml-1 font-mono text-xs opacity-60">{name}</span>
                                </span>
                            </button>
                        ))
                    )}
                </PopoverPrimitive.Content>
            </PopoverPrimitive.Portal>
        </PopoverPrimitive.Root>
    );
};
