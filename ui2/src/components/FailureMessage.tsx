import { AlertCircle } from 'lucide-react';

export const FailureMessage = ({ invocation }: any) => {
    const ex = invocation.executionException;
    if (!ex || !ex.message) return null;

    return (
        <div className="my-4 rounded-lg border border-rose-200 bg-rose-50/50 overflow-hidden">
            <div className="flex items-start gap-3 p-4">
                <AlertCircle className="text-rose-600 shrink-0 mt-0.5" size={18} />
                <div className="space-y-2">
                    <p className="text-sm font-bold text-rose-900 leading-tight">
                        {ex.message}
                    </p>
                    {ex.stackTrace && (
                        <details className="group">
                            <summary className="text-[10px] font-black uppercase tracking-widest text-rose-500 cursor-pointer hover:text-rose-700 transition-colors list-none">
                                View Stacktrace
                            </summary>
                            <pre className="mt-3 p-4 bg-rose-900 text-rose-100 text-[11px] font-mono rounded-md overflow-auto max-h-[300px] shadow-inner selection:bg-rose-500/30">
                                {ex.stackTrace}
                            </pre>
                        </details>
                    )}
                </div>
            </div>
        </div>
    );
};