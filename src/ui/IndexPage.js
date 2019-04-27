import React, {Component} from 'react';
import './IndexPage.scss';
import App from "./App";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

class Test extends Component {
    render() {
        let test = this.props.test;
        return (
                <div className={"message is-small " + App.stateClassFor(test.state)}>
                    <div className="message-body link" onClick={() => this.props.load(test.testMethod)}>{test.displayName}</div>
                </div>
        )
    }
}

class Entry extends Component {

    constructor(props) {
        super(props);

        this.state = {
            entry: this.props.entry,
            isCollapsed: true
        };

        this.toggle = this.toggle.bind(this);
        this.load = this.load.bind(this);
    }

    toggle() {
        this.setState(prevState => ({
            isCollapsed: !prevState.isCollapsed
        }));
    }

    load(anchor) {
        let url = "./" + this.state.entry.testClass + ".html";
        if (anchor) {
            url += "#" + anchor;
        }
        window.location = url;
    }

    icon() {
        if (this.state.isCollapsed) {
            return faAngleDown;
        }

        return faAngleUp;
    }

    contentClass() {
        let c = "message-body";
        if (this.state.isCollapsed) {
            return c + " is-hidden"
        }

        return c;
    }

    render() {
        let entry = this.state.entry;
        return (
                <div className={"index-entry message " + App.stateClassFor(entry.state)}>
                    <div className="message-header">
                        <div className="link" onClick={() => this.load()}>{entry.displayName}</div>
                        <a onClick={this.toggle}>
                            <FontAwesomeIcon icon={this.icon()}/>
                        </a>
                    </div>
                    <div className={this.contentClass()}>
                        {entry.tests.map((test, index) => <Test key={index} test={test} load={this.load}/>)}
                    </div>
                </div>
        )
    }
}

export default class Indices extends Component {

    render() {
        return (
                <div>
                    {this.props.indices.map((entry, index) =>
                            <Entry key={index} entry={entry}/>
                    )}
                </div>
        );
    }
}