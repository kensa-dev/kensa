import React from "react";

export function NamedValueTable({showHeader, highlights, namedValues}) {
    
    const highlightClassFor = (name) => {
        const descriptor = highlights.find(entry => entry.name === name);
        return descriptor && (`kensa-highlight kensa-highlight-${descriptor.colourIndex}`);
    }

    return (
        <table className="table is-bordered is-striped is-fullwidth">
            {
                showHeader &&
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Value</th>
                </tr>
                </thead>
            }
            <tbody>
            {
                namedValues.map((item, idx) =>
                    Object.entries(item).map(([name, value]) =>
                        <tr key={idx}>
                            <td>{name}</td>
                            <td>
                                <span className={highlightClassFor(name)}>{value}</span>
                            </td>
                        </tr>
                    )
                )
            }
            </tbody>
        </table>
    );
}