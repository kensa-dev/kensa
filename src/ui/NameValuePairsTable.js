import React, {Component} from "react";

export class NameValuePairsTable extends Component {
    render() {
        const nameValuePairs = this.props.nameValuePairs;
        return (
                <table className="table is-striped is-fullwidth">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                    </tr>
                    </thead>
                    <tbody>
                    {nameValuePairs.map((item, index) =>
                            <tr key={index}>
                                <td>{item.name}</td>
                                <td>{item.value}</td>
                            </tr>
                    )}
                    </tbody>
                </table>
        );
    }
}