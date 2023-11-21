import React, {Component} from "react";
import {Invocation} from "./Invocation";
import App, {makeNotes} from "./App";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

class Test extends Component {

    constructor(props) {
        super(props);

        let test = this.props.test;
        let state = test.state;
        let testMethod = test.testMethod;
        let isCollapsed = true;

        if (window.location.hash) {
            isCollapsed = window.location.hash.substring(1) !== testMethod
        } else {
            isCollapsed = state !== "Failed" && state !== "Disabled"
        }

        this.state = {
            isCollapsed: isCollapsed
        };

        this.toggle = this.toggle.bind(this);

    }

    toggle() {
        this.setState(prevState => ({
            isCollapsed: !prevState.isCollapsed
        }));
    }

    icon() {
        if (this.state.isCollapsed) {
            return faAngleDown;
        }

        return faAngleUp;
    }

    contentClass() {
        if (this.state.isCollapsed) {
            return "message-body is-hidden"
        }

        return "message-body";
    }

    renderInformation(issue, notes) {
        let issueContent = issue.length > 0 ? this.renderIssues(issue) : null;
        let notesContent = notes ? makeNotes(notes) : null;

        if (issue.length > 0 || notes) {
            return (
                <div className="message-body is-info">
                    {issueContent}
                    {notesContent}
                </div>
            )
        }

        return null;
    }

    renderIssues(issues) {
        return (
            <div className="tags">
                {
                    issues.map(issue => {
                        return <a href={this.issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                    })
                }
            </div>
        );
    }

    issueTrackerUrlFor(issue) {
        let issueTrackerUrl = this.props.issueTrackerUrl;

        if (issueTrackerUrl.endsWith("/")) {
            return issueTrackerUrl + issue;
        }

        return issueTrackerUrl + "/" + issue;
    }

    render() {
        const test = this.props.test;
        const state = test.state;
        if (state === 'Disabled') {
            return (
                <div className={"message " + App.stateClassFor(state)}>
                    <div className="message-header">{test.displayName}</div>
                    <div className="message-body">
                        Test was not executed.
                    </div>
                </div>
            );
        } else {

            let expandedIndex = test.invocations.findIndex((invocation) => invocation.state === 'Failed')
            if(expandedIndex === -1) expandedIndex = 0

            return (
                <div className={"message " + App.stateClassFor(state)}>
                    <div onClick={this.toggle} className="message-header">
                        {test.displayName}
                        <a><FontAwesomeIcon icon={this.icon()}/></a>
                    </div>
                    <div className={this.contentClass()}>
                        {this.renderInformation(test.issue, test.notes)}
                        {test.invocations.map((invocation, index) => <Invocation key={index}
                                                                                 sectionOrder={this.props.sectionOrder}
                                                                                 testMethod={test.testMethod}
                                                                                 isCollapsed={index !== expandedIndex}
                                                                                 invocation={invocation}
                                                                                 invocationNumber={index}/>)}
                    </div>
                </div>
            );
        }
    }
}

export default class TestWrapper extends Component {
    render() {
        return (
            this.props.tests.map((test, index) =>
                <Test issueTrackerUrl={this.props.issueTrackerUrl} sectionOrder={this.props.sectionOrder} key={index} test={test}/>
            )
        );
    }
}