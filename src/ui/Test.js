import React, {Component} from "react";
import {Invocation} from "./Invocation";
import App from "./App";
import ScrollableAnchor from "react-scrollable-anchor";
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

    renderInformation(testMethod, issue, notes, parameters) {
        let renderedParameters = (
            <table className="table">
                {this.renderParameterNames(parameters)}
                {this.renderParameterValues(parameters, testMethod)}
            </table>
        );

        let notesContent = notes ? <div>{notes}</div> : null;

        if (issue.length > 0 || notes || parameters.length > 0) {
            return (
                <div className="message-body">
                    {this.renderIssues(issue)}
                    {notesContent}
                    {renderedParameters}
                </div>
            )
        }

        return null;
    }

    renderParameterValues(parameters, testMethod) {
        return parameters.length > 0 ?
            parameters.map((testParameters, index) => {
                return (<tr>{
                    testParameters.map(parameter => {
                        let value = Object.values(parameter)[0];
                        let link = "#" + this.parameterisedTestResourceFor(testMethod, index);
                        return (<td><a href={link}>{value}</a></td>);
                    })}</tr>);
            })
            : null;
    }

    parameterisedTestResourceFor(testMethod, index) {
        return testMethod + "_" + index;
    }

    renderParameterNames(parameters) {
        return parameters.length > 0 ? (
                <tr>
                    {parameters[0].map(firstParameter => {
                        return (<th>{Object.keys(firstParameter)[0]}</th>);
                    })}
                </tr>
            )
            : null;
    }

    renderIssues(issues) {
        return issues.length > 0 ? (
                <div className="tags">
                    {
                        issues.map(issue => {
                            return <a href={this.issueTrackerUrlFor(issue)}
                                      className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                        })
                    }
                </div>
            )
            : null;
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
            let params = test.invocations.map(inv => {
                return inv.parameters
            });
            return (
                <ScrollableAnchor id={test.testMethod}>
                    <div className={"message " + App.stateClassFor(state)}>
                        <div onClick={this.toggle} className="message-header">
                            {test.displayName}
                            <a><FontAwesomeIcon icon={this.icon()}/></a>
                        </div>
                        <div className={this.contentClass()}>
                            {this.renderInformation(test.testMethod, test.issue, test.notes, params)}
                            {test.invocations.map((invocation, index) => <ScrollableAnchor
                                id={this.parameterisedTestResourceFor(test.testMethod, index)}>
                                <Invocation key={index}
                                            testMethod={test.testMethod}
                                            invocation={invocation}
                                            invocationNumber={index}/></ScrollableAnchor>)}
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