import React, {Component} from "react";
import {highlightPlainText} from "./Highlighting";
import {joinForRegex} from "./Util";

export class NamedValueTable extends Component {
    setCodeRef(wrappingDiv) {
        if (wrappingDiv) {
            let highlightRegexp = this.props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;
            if (highlightRegexp) {
                highlightPlainText(wrappingDiv, highlightRegexp)
            }
        }
    }

    render() {
        const namedValues = this.props.namedValues;
        return (
            <table className="table is-bordered is-striped is-fullwidth">
                {
                    this.props.showHeader &&
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                    </tr>
                    </thead>
                }
                <tbody>
                {namedValues.map((item, index) => {
                        let name = Object.keys(item)[0];
                        let value = item[name];
                        return <tr key={index}>
                            <td>{name}</td>
                            <td>
                                <div ref={this.setCodeRef.bind(this)}>{value}</div>
                            </td>
                        </tr>
                    }
                )}
                </tbody>
            </table>
        );
    }
}