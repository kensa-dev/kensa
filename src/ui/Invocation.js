import React, {Component, useState} from "react";
import {Sentence} from "./Sentence";
import {SequenceDiagram} from "./SequenceDiagram";
import {NamedValueTable} from "./NamedValueTable";
import {CapturedInteractions} from "./Interaction";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {Section, stateClassFor} from "./App";

class StackTracePopup extends Component {
    constructor(props) {
        super(props);

        this.fontSizes = ["font-normal", "font-large"]
        this.state = {
            fontSizeIdx: 0,
        };

        this.onKeyDown = this.onKeyDown.bind(this)
    }

    onKeyDown(event) {
        if (event.keyCode === 27) {
            this.props.onHide();
        }
    }

    componentDidMount() {
        document.addEventListener("keydown", this.onKeyDown, false);
    }

    componentWillUnmount() {
        document.removeEventListener("keydown", this.onKeyDown, false);
    }

    render() {
        return (
            <div className={"modal is-active"}>
                <div className="modal-background" onClick={this.props.onHide}/>
                <div className="modal-card">
                    <header className="modal-card-head">
                        <p className="modal-card-title">Stacktrace</p>
                        <a onClick={this.props.onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                    </header>
                    <section className={"modal-card-body " + this.fontSizes[this.state.fontSizeIdx]}>
                        <div className="stack-trace">{this.props.stackTrace}</div>
                    </section>
                </div>
            </div>
        )
    }
}

export default function Invocation({sectionOrder, testMethod, isParentCollapsed, invocation}) {
    const [selectedTab, setSelectedTab] = useState(null)
    const [isStackTracePopupActive, setStackTracePopupActive] = useState(false)
    const [isCollapsed, setCollapsed] = useState(isParentCollapsed)

    const selectTab = (tabName) => {
        setSelectedTab(selectedTab === tabName ? null : tabName)
    }

    const classForButton = (buttonName, testStateClass) => {
        let c = "button ";
        if (selectedTab === buttonName) {
            c += "is-selected " + testStateClass;
        } else if (selectedTab !== null && selectedTab !== undefined) {
            c += " has-selected"
        }

        return c;
    }

    const classForContentBody = (name) => {
        if (selectedTab === name) {
            return "";
        }

        return "is-hidden"
    }

    const hasElements = (name) => {
        let invocationElement = invocation[name];
        return invocationElement && invocationElement.length > 0;
    }

    const buttonFor = (key, name, text) => {
        if (hasElements(name)) {
            return <button key={key} className={classForButton(name, stateClassFor(invocation.state))} onClick={() => selectTab(name)}>{text}</button>;
        }
    }

    const contentFor = (name, component) => {
        if (hasElements(name)) {
            return component;
        }
    }

    const failureMessageBlock = (executionException) => {
        if (executionException["message"]) {
            return (<div key={2}>
                <div className="failure-message">{executionException.message}</div>
                <button className="button is-danger is-small" onClick={() => showStackTracePopup()}>Stacktrace...</button>
            </div>)
        }

        return null;
    }

    const showStackTracePopup = () => {
        setStackTracePopupActive(true)
    }

    const hideStackTracePopup = () => {
        setStackTracePopupActive(false)
    }

    const renderStackTracePopup = (stackTrace) => {
        if (isStackTracePopupActive) {
            return <StackTracePopup onHide={hideStackTracePopup} stackTrace={stackTrace}/>
        }
    }

    const buttons = (index) => {
        let highlights = invocation.highlights;

        if (hasElements('givens') ||
            hasElements('parameters') ||
            hasElements('capturedInteractions') ||
            hasElements('sequenceDiagram')) {
            return <div>
                <div key={1} className="buttons has-addons">
                    {buttonFor('b' + 1, 'givens', 'Givens')}
                    {buttonFor('b' + 2, 'parameters', 'Parameters')}
                    {buttonFor('b' + 3, 'capturedInteractions', 'Captured Interactions')}
                    {buttonFor('b' + 4, 'sequenceDiagram', 'Sequence Diagram')}
                </div>
                {contentFor('givens', <div key={2} className={classForContentBody('givens')}><NamedValueTable showHeader={true} highlights={highlights}
                                                                                                              namedValues={invocation.givens}/></div>)}
                {contentFor('parameters', <div key={3} className={classForContentBody('parameters')}><NamedValueTable showHeader={true} highlights={highlights}
                                                                                                                      namedValues={invocation.parameters}/></div>)}
                {contentFor('capturedInteractions', <div key={4} className={classForContentBody('capturedInteractions')}><CapturedInteractions
                    invocationState={invocation.state} capturedInteractions={invocation.capturedInteractions} highlights={highlights}/></div>)}
                {contentFor('sequenceDiagram', <div key={5} className={classForContentBody('sequenceDiagram')}><SequenceDiagram sequenceDiagram={invocation.sequenceDiagram}
                                                                                                                                capturedInteractions={invocation.capturedInteractions}
                                                                                                                                invocationState={invocation.state}
                                                                                                                                highlights={invocation.highlights}/>
                </div>)}
            </div>
        }

        return null;
    }

    const sentences = (invocation) => {
        return (
            <div key={3} className="sentences">
                {invocation.sentences.map((sentence, index) => <Sentence key={index} expanded={false} sentence={sentence} acronyms={invocation.acronyms}/>)}
            </div>
        )
    }

    const toggle = () => {
        setCollapsed(!isCollapsed)
    }

    const icon = () => {
        if (isCollapsed) {
            return faAngleDown;
        }

        return faAngleUp;
    }

    const contentClass = () => {
        if (isCollapsed) {
            return " is-hidden"
        }

        return "";
    }

    const renderTestInvocation = (testBody) => {
        if (!hasElements('parameters')) {
            return testBody
        }

        return null
    }

    const lazyBody = (testBody) => {
        if (!isCollapsed) {
            return (<div className={"message-body " + contentClass()}>
                {testBody}
            </div>)
        } else return null
    }

    const renderParameterizedTestInvocation = (testBody, invocation, testStateClass) => {
        if (hasElements('parameters')) {
            return (
                <div key={1} className={"message " + testStateClass}>
                    <div onClick={toggle} className={"message-header"}>
                        <span className={"limited-width"}>{invocation.parameterizedTestDescription}</span>
                        <div>
                            <span className={"elapsed-time"}>Elapsed time: {invocation.elapsedTime}</span>
                            <a><FontAwesomeIcon icon={icon()}/></a>
                        </div>
                    </div>
                    {lazyBody(testBody)}
                </div>
            )
        }

        return null
    }

    let testStateClass = stateClassFor(invocation.state);
    let testBody = <>
        {
            sectionOrder.map((section, index) => {
                    switch (section) {
                        case Section.Buttons:
                            return buttons(invocation, index)
                        case Section.Exception:
                            return failureMessageBlock(invocation.executionException, index)
                        case Section.Sentences:
                            return sentences(invocation, index)
                    }
                }
            )
        }
    </>

    return <>
        {renderParameterizedTestInvocation(testBody, invocation, testStateClass)}
        {renderTestInvocation(testBody)}
        {renderStackTracePopup(invocation.executionException.stackTrace)}
    </>
}