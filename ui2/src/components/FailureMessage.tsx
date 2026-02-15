import { AlertCircle } from 'lucide-react';

export const FailureMessage = ({ invocation }: any) => {
    const ex = invocation.executionException;
    if (!ex || !ex.message) return null;

    return (
        <div className="my-4 rounded-lg border border-failure-10 bg-failure-2 overflow-hidden">
            <div className="flex items-start gap-3 p-4">
                <AlertCircle className="text-failure shrink-0 mt-0.5" size={18} />
                <div className="space-y-2">
                    <p className="text-sm font-bold text-failure leading-tight">
                        {ex.message}
                    </p>
                    {ex.stackTrace && (
                        <details className="group">
                            <summary className="text-[10px] font-black uppercase tracking-widest text-failure cursor-pointer hover:text-failure transition-colors list-none">
                                View Stacktrace
                            </summary>
                            <pre className="mt-3 p-4 bg-failure-10 text-failure text-[11px] font-mono rounded-md overflow-auto max-h-[300px] shadow-inner selection:bg-failure-10">
                                {ex.stackTrace}
                            </pre>
                        </details>
                    )}
                </div>
            </div>
        </div>
    );
};