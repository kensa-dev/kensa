import React, {Component} from 'react';

class Index extends Component {
    navigateTo(url) {
        window.location = url;
    }

    render() {
        let entry = this.props.entry;
        return (
                <span className="columns is-success has-background-success" onClick={() => this.navigateTo("./" + entry.testClass + ".html")}>
                    <div className="column">{entry.displayName}</div>
                    <div className="column">{entry.state}</div>
                </span>
        )
    }
}

export default class MultiPageIndex extends Component {

    render() {
        return (
                <div className="">
                    {this.props.indices.map((entry, index) =>
                            <Index key={index} entry={entry}/>
                    )}
                </div>
        );
    }
}