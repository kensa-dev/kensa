import React, {Component} from "react";
import {Invocation} from "./Invocation";
import App from "./App";
import ScrollableAnchor from "react-scrollable-anchor";

class Test extends Component {

    renderInformation(issue, notes) {
        let issueContent = issue.length > 0 ? this.renderIssues(issue) : null;
        let notesContent = notes ? <div>{notes}</div> : null;

        if (issue.length > 0 || notes) {
            return (
                    <div className="message-body">
                        {issueContent}
                        {notesContent}
                    </div>
            )
        }

        return null;
    }

    renderIssues(issues) {
        return <div className="tags">
            {
                issues.map(issue => {
                    return <a href={this.issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                })
            }
        </div>;
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
                    <ScrollableAnchor id={test.testMethod}>
                        <div className={"message " + App.stateClassFor(state)}>
                            <div className="message-header">{test.displayName}</div>
                            <div className="message-body">
                                Test was not executed.
                            </div>
                        </div>
                    </ScrollableAnchor>
            );
        } else {
            return (
                    <ScrollableAnchor id={test.testMethod}>
                        <div className={"message " + App.stateClassFor(state)}>
                            <div className="message-header">
                                {test.displayName}
                            </div>
                            <div className="message-body">
                                {this.renderInformation(test.issue, test.notes)}
                                {test.invocations.map((invocation, index) => <Invocation key={index} testMethod={test.testMethod} invocation={invocation} invocationNumber={index}/>)}
                            </div>
                        </div>
                    </ScrollableAnchor>
            );
        }
    }
}

export default class TestWrapper extends Component {
    render() {
        return (
                this.props.tests.map((test, index) =>
                        <Test issueTrackerUrl={this.props.issueTrackerUrl} key={index} test={test}/>
                )
        );
    }
}