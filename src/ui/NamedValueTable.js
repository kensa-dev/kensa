import React, {Component} from "react";

export class NamedValueTable extends Component {
    render() {
        const namedValues = this.props.namedValues;
        const nameFunction = this.props.nameFunction ? this.props.nameFunction : this.nameFrom;
        const valueFunction = this.props.valueFunction ? this.props.valueFunction : this.valueFrom;
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
                                        let name = nameFunction(item);
                                        let value = valueFunction(item);
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

    nameFrom(item) {
        return Object.keys(item)[0];
    }

    valueFrom(item) {
        return Object.values(item)[0];
    }
}