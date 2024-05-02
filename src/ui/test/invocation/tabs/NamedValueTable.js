import React from "react";
import {highlightPlainText} from "../Highlighting";
import {joinForRegex} from "../../../Util";

export function NamedValueTable({showHeader, highlights, namedValues}) {
    const setCodeRef = (wrappingDiv) => {
        if (wrappingDiv) {
            let highlightRegexp = highlights.length > 0 ? new RegExp(`(${joinForRegex(highlights)})`) : null;
            if (highlightRegexp) {
                highlightPlainText(wrappingDiv, highlightRegexp)
            }
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