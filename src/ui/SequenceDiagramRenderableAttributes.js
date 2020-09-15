import React, {Component} from "react";
import {highlightPlainText} from "./Highlighting";

export class SequenceDiagramRenderableAttributes extends Component {

    setCodeRef(wrappingDiv) {
        if (wrappingDiv) {
            let highlightRegexp = this.props.highlights.length > 0 ? new RegExp(`(${this.props.highlights.join('|')})`) : null;
            if (highlightRegexp) {
                highlightPlainText(wrappingDiv, highlightRegexp)
            }
        }
    }

    render() {
        const attributes = this.props.attributes;
        return (
            <div>
                <table className="table pairs is-borderless is-narrow">
                    <tbody>
                    {attributes
                        .map((attribute) => {
                                if (attribute["showOnSequenceDiagram"]) {
                                    let attributeName = Object.keys(attribute)[1];
                                    return attribute[attributeName].map((item, index) => {
                                        let itemName = Object.keys(item)[0];
                                        let value = item[itemName];
                                        return (<tr key={index}>
                                            <td>{itemName}</td>
                                            <td>
                                                <div ref={this.setCodeRef.bind(this)}>{value}</div>
                                            </td>
                                        </tr>);
                                    })

                                }
                            }
                        )}
                    </tbody>
                </table>
                {attributes.length > 0 ? <div className="is-divider"/> : null}
            </div>
        );
    }
}