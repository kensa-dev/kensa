import React, {Component} from "react";
import {Invocation} from "./Invocation";
import App from "./App";

class Test extends Component {

    renderInformation(issue, notes, state) {
        let issueContent = issue ? this.renderIssues(issue, state) : null;

        if (issue || notes) {
            return (
                    <div className="message-body">
                        {issueContent}
                        <div>{notes}</div>
                    </div>
            )
        }

        return null;
    }

    renderIssues(issues, state) {
        let stateClass = App.darkerStateClassFor(state);

        return <div className="tags">
            {
                issues.map(issue => {
                    return <a href={this.issueTrackerUrlFor(issue)} className={"tag is-medium " + stateClass}>{issue}</a>
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
                    <div className={"message " + App.stateClassFor(state)}>
                        <div className="message-header">{test.displayName}</div>
                        <div className="message-body">
                            Test was not executed.
                        </div>
                    </div>
            );
        } else {
            return (
                    <div className={"message " + App.stateClassFor(state)}>
                        <div className="message-header">
                            {test.displayName}
                        </div>
                        <div className="message-body">
                            {this.renderInformation(test.issue, test.notes, state)}
                            {test.invocations.map((invocation, index) => <Invocation key={index} testMethod={test.testMethod} invocation={invocation} invocationNumber={index}/>)}
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
                        <Test issueTrackerUrl={this.props.issueTrackerUrl} key={index} test={test}/>
                )
        );
    }
}