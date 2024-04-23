import React, {useState} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {makeNotes, stateClassFor} from "./App";
import Invocation from "./Invocation";

function Test({issueTrackerUrlFor, sectionOrder, test}) {
    let testState = test.state
    let testMethod = test.testMethod
    let collapsed = false
    if (window.location.hash) {
        collapsed = window.location.hash.substring(1) !== testMethod
    } else collapsed = testState !== "Failed"

    const [isCollapsed, setIsCollapsed] = useState(collapsed)

    const toggle = () => {
        setIsCollapsed(!isCollapsed);
    }

    const icon = () => {
        return isCollapsed ? faAngleDown : faAngleUp
    }

    const contentClass = () => {
        if (isCollapsed) {
            return "message-body is-hidden"
        }

        return "message-body"
    }

    const renderInformation = (issues, notes) => {
        let issueContent = issues.length > 0 ? renderIssues(issues) : null;
        let notesContent = notes ? makeNotes(notes) : null;

        if (issues.length > 0 || notes) {
            return (
                <div className="test-information">
                    {issueContent}
                    {notesContent}
                </div>
            )
        }

        return null;
    }

    const renderIssues = (issues) => {
        return (
            <div className="tags">
                {
                    issues.map(issue => {
                        return <a href={issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                    })
                }
            </div>
        );
    }

    if (testState === 'Disabled' || testState === 'Not Executed') {
        return (
            <div className={"message " + stateClassFor(testState)}>
                <div onClick={toggle} className="message-header">
                    <span className={"limited-width"}>{test.displayName}</span>
                    <a><FontAwesomeIcon icon={icon()}/></a>
                </div>
                <div className={"message-body " + contentClass()}>
                    Test was not executed.
                </div>
            </div>
        );
    } else {
        let expandedIndex = test.invocations.findIndex((invocation) => invocation.state === 'Failed')
        if (expandedIndex === -1) expandedIndex = 0
        return (
            <div className={"message " + stateClassFor(testState)}>
                <div onClick={toggle} className="message-header">
                    <span className={"limited-width"}>{test.displayName}</span>
                    <div>
                        <span className={"elapsed-time"}>Elapsed time: {test.elapsedTime}</span>
                        <a><FontAwesomeIcon icon={icon()}/></a>
                    </div>
                </div>
                <div className={contentClass()}>
                    {renderInformation(test.issues, test.notes)}
                    {test.invocations.map((invocation, index) => <Invocation sectionOrder={sectionOrder}
                                                                             testMethod={test.testMethod}
                                                                             isCollapsed={index !== expandedIndex}
                                                                             invocation={invocation}
                                                                             key={index}/>)}
                </div>
            </div>
        );
    }
}

export default function TestWrapper({issueTrackerUrlFor, sectionOrder, tests}) {
    const components = tests.map((test, index) =>
        <Test issueTrackerUrlFor={issueTrackerUrlFor} sectionOrder={sectionOrder} test={test} key={index}/>
    )
    // debugger;
    return (
        <>{components}</>
    )
}