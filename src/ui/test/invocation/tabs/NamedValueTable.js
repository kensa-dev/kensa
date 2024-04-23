import React from "react";
import {highlight} from "../Highlighting";

export function NamedValueTable({showHeader, highlights, namedValues}) {
    const setCodeRef = (wrappingDiv) => {
        if(wrappingDiv) {
            highlight('plainText', wrappingDiv, highlights)
        }
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
                                <div ref={setCodeRef.bind(this)}>{value}</div>
                            </td>
                        </tr>
                    )
                )
            }
            </tbody>
        </table>
    );
}