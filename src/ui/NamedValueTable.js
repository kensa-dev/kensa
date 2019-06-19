import React, {Component} from "react";

export class NamedValueTable extends Component {
    render() {
        const namedValues = this.props.namedValues;
        return (
                <table className="table is-striped is-fullwidth">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                    </tr>
                    </thead>
                    <tbody>
                    {namedValues
                            .map((item, index) => {
                                        let name = Object.keys(item)[0];
                                        let value = item[name];
                                        return <tr key={index}>
                                            <td>{name}</td>
                                            <td>
                                                <div className={this.classFor(value)}>{value}</div>
                                            </td>
                                        </tr>
                                    }
                            )}
                    </tbody>
                </table>
        );
    }

    classFor(value) {
        let highlights = this.props.highlights;
        let c = "";

        if (highlights) {
            highlights.forEach(highlight => {
                        if (highlight === value) {
                            c = "kensa-highlight";
                        }
                    }
            );
        }

        return c;
    }
}