import React, {Component} from "react";
import App, {Section} from './App';
import {Sentence} from "./Sentence";
import {SequenceDiagram} from "./SequenceDiagram";
import {NamedValueTable} from "./NamedValueTable";
import {CapturedInteractions} from "./Interaction";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";

class ExecutionException extends Component {

    constructor(props) {
        super(props);

        this.state = {
            showingStacktrace: false,
            buttonText: 'Show Stacktrace'
        };

        this.toggleStacktrace = this.toggleStacktrace.bind(this);

    }

    toggleStacktrace() {
        this.setState(prevState => ({
            showingStacktrace: !prevState.showingStacktrace,
            buttonText: prevState.showingStacktrace ? "Show Stacktrace" : "Hide Stacktrace"
        }));
    }

    isHidden() {
        return this.state.showingStacktrace ? "" : "is-hidden";
    }

    render() {
        let executionException = this.props.executionException;

        return (
            <div className="execution-exception">
                <div className="button is-outlined test-failed" onClick={this.toggleStacktrace}>{this.state.buttonText}</div>
                <div className="exception-message">{executionException.message}</div>
                <textarea readOnly className={"test-failed textarea exception-stacktrace " + this.isHidden()}>{executionException.stackTrace}</textarea>
            </div>
        );
    }
}

export class Invocation extends Component {

    constructor(props) {
        super(props);

        this.state = {
            index: props.index,
            invocation: props.invocation,
            invocationNumber: props.invocationNumber,
            testMethod: props.testMethod,
            isCollapsed: props.isCollapsed
        };

        this.toggle = this.toggle.bind(this);
        this.selectTab = this.selectTab.bind(this);
        this.classForButton = this.classForButton.bind(this);
        this.classForContentBody = this.classForContentBody.bind(this);
        this.isDisabled = this.isDisabled.bind(this);
    }

    selectTab(tabName) {
        this.setState(prevState => ({
            selectedTab: prevState.selectedTab === tabName ? null : tabName
        }));
    }

    isDisabled(buttonName) {
        return this.state.invocation[buttonName].length === 0;
    }

    classForButton(buttonName, testStateClass) {
        let c = "button ";
        if (this.state.selectedTab === buttonName) {
            c += "is-selected " + testStateClass;
        } else if (this.state.selectedTab !== null && this.state.selectedTab !== undefined) {
            c += " has-selected"
        }

        return c;
    }

    classForContentBody(name) {
        if (this.state.selectedTab === name) {
            return "";
        }

        return "is-hidden"
    }

    buttonFor(name, text) {
        if (this.hasElements(name)) {
            return <button className={this.classForButton(name, App.stateClassFor(this.state.invocation.state))} onClick={() => this.selectTab(name)}>{text}</button>;
        }
    }

    contentFor(name, Component) {
        if (this.hasElements(name)) {
            return Component;
        }
    }

    exceptionBlock(executionException) {
        if (executionException["message"]) {
            return (<div key={2} className="message-body has-text-black test-failed">
                <ExecutionException executionException={executionException}/>
            </div>)
        }

        return null;
    }

    hasElements(name) {
        let invocationElement = this.state.invocation[name];
        return invocationElement && invocationElement.length > 0;
    }


    buttons(invocation, testStateClass) {
        let highlights = invocation.highlights;

        if (this.hasElements('givens') ||
            this.hasElements('parameters') ||
            this.hasElements('capturedInteractions') ||
            this.hasElements('sequenceDiagram')) {
            return <div>
                <span className={"tag is-pulled-right " + testStateClass}>Executed in: {invocation.elapsedTime}</span>
                <div className="buttons has-addons">
                    {this.buttonFor('givens', 'Givens')}
                    {this.buttonFor('parameters', 'Parameters')}
                    {this.buttonFor('capturedInteractions', 'Captured Interactions')}
                    {this.buttonFor('sequenceDiagram', 'Sequence Diagram')}
                </div>
                {this.contentFor('givens', <div className={this.classForContentBody('givens')}><NamedValueTable showHeader={true} highlights={highlights}
                                                                                                                namedValues={invocation.givens}/></div>)}
                {this.contentFor('parameters', <div className={this.classForContentBody('parameters')}><NamedValueTable showHeader={true} highlights={highlights}
                                                                                                                        namedValues={invocation.parameters}/></div>)}
                {this.contentFor('capturedInteractions', <div className={this.classForContentBody('capturedInteractions')}><CapturedInteractions
                    invocationState={this.state.invocation.state} capturedInteractions={invocation.capturedInteractions} highlights={highlights}/></div>)}
                {this.contentFor('sequenceDiagram', <div className={this.classForContentBody('sequenceDiagram')}><SequenceDiagram sequenceDiagram={invocation.sequenceDiagram}
                                                                                                                                  capturedInteractions={invocation.capturedInteractions}
                                                                                                                                  invocationState={invocation.state}
                                                                                                                                  highlights={invocation.highlights}/>
                </div>)}
            </div>
        }

        return null;
    }

    sentences(invocation) {
        return (
            <div key={3} className="sentences">
                {invocation.sentences.map((sentence, index) => <Sentence key={index} expanded={false} sentence={sentence} acronyms={invocation.acronyms}/>)}
            </div>
        )
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
            return " is-hidden"
        }

        return "";
    }

    renderTestInvocation(testBody) {
        if (!this.hasElements('parameters')) {
            return testBody
        }

        return null
    }

    foopy(testBody) {
        if(!this.state.isCollapsed) {
            return (<div className={"message-body " + this.contentClass()}>
                {testBody}
            </div>)
        } else return null
    }

    renderParameterizedTestInvocation(testBody, invocation, testStateClass) {
        if (this.hasElements('parameters')) {
            return (
                <div className={"message " + testStateClass}>
                    <div onClick={this.toggle} className={"message-header"}>
                        {invocation.parameterizedTestDescription}
                        <a><FontAwesomeIcon icon={this.icon()}/></a>
                    </div>
                    { this.foopy(testBody) }
                </div>
            )
        }

        return null
    }

    render() {
        let invocation = this.state.invocation;
        let testStateClass = App.stateClassFor(invocation.state);
        let testBody = <React.Fragment>
            {
                this.props.sectionOrder.map((section) => {
                        switch (section) {
                            case Section.Buttons:
                                return this.buttons(invocation, testStateClass)
                            case Section.Exception:
                                return this.exceptionBlock(invocation.executionException)
                            case Section.Sentences:
                                return this.sentences(invocation)
                        }
                    }
                )
            }
        </React.Fragment>

        return <React.Fragment>
            {this.renderParameterizedTestInvocation(testBody, invocation, testStateClass)}
            {this.renderTestInvocation(testBody)}
        </React.Fragment>
    }
}

