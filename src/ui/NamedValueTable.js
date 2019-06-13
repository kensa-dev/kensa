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
                    {namedValues.map((item, index) =>
                            <tr key={index}>
                                <td>{item.name}</td>
                                <td>
                                    <div className={this.classFor(item.value)}>{item.value}</div>
                                </td>
                            </tr>
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