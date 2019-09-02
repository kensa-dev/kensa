import React, {Component} from "react";
import App from './App';
import {Sentence} from "./Sentence";
import {SequenceDiagram} from "./SequenceDiagram";
import {NamedValueTable} from "./NamedValueTable";
import {CapturedInteractions} from "./Interaction";

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
            invocation: props.invocation,
            invocationNumber: props.invocationNumber,
            testMethod: props.testMethod,
            selectedTab: 'givens'
        };

        this.selectTab = this.selectTab.bind(this);
        this.classForButton = this.classForButton.bind(this);
        this.classForContentBody = this.classForContentBody.bind(this);
        this.isDisabled = this.isDisabled.bind(this);
    }

    selectTab(tabName) {
        this.setState({
            selectedTab: tabName
        });
    }

    isDisabled(buttonName) {
        return this.state.invocation[buttonName].length === 0;
    }

    classForButton(buttonName, testStateClass) {
        let c = "button ";
        if (this.state.selectedTab === buttonName) {
            c += "is-selected " + testStateClass;
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
        let invocationElement = this.state.invocation[name];
        if (invocationElement && invocationElement.length > 0) {
            return <button className={this.classForButton(name, App.stateClassFor(this.state.invocation.state))} onClick={() => this.selectTab(name)}>{text}</button>;
        }
    }

    contentFor(name, Component) {
        let invocationElement = this.state.invocation[name];
        if (invocationElement && invocationElement.length > 0) {
            return Component;
        }
    }

    exceptionBlock(executionException) {
        if (executionException["message"]) {
            return (<div className="message-body has-text-black test-failed">
                <ExecutionException executionException={executionException}/>
            </div>)
        }

        return null;
    }

    render() {
        let invocation = this.state.invocation;
        let testStateClass = App.stateClassFor(invocation.state);
        let highlights=invocation.highlights;

        return (
                <div className={"message " + testStateClass}>
                    <div className="message-body">
                        <span className={"tag is-pulled-right " + testStateClass}>Executed in: {invocation.elapsedTime}</span>
                        <div className="buttons has-addons">
                            {this.buttonFor('givens', 'Givens')}
                            {this.buttonFor('parameters', 'Parameters')}
                            {this.buttonFor('capturedInteractions', 'Captured Interactions')}
                            {this.buttonFor('sequenceDiagram', 'Sequence Diagram')}
                        </div>
                        {this.contentFor('givens', <div className={this.classForContentBody('givens')}><NamedValueTable highlights={highlights} namedValues={invocation.givens}/></div>)}
                        {this.contentFor('parameters', <div className={this.classForContentBody('parameters')}><NamedValueTable highlights={highlights} namedValues={invocation.parameters}/></div>)}
                        {this.contentFor('capturedInteractions', <div className={this.classForContentBody('capturedInteractions')}><CapturedInteractions
                            capturedInteractions={invocation.capturedInteractions} highlights={highlights}/></div>)}
                        {this.contentFor('sequenceDiagram', <div className={this.classForContentBody('sequenceDiagram')}><SequenceDiagram sequenceDiagram={invocation.sequenceDiagram} capturedInteractions={invocation.capturedInteractions} highlights={invocation.highlights}/>
                        </div>)}
                    </div>
                    <div className="message-body has-text-black">
                        {invocation.sentences.map((sentence, index) => <Sentence key={index} expanded={false} sentence={sentence} acronyms={invocation.acronyms}/>)}
                    </div>
                    {this.exceptionBlock(invocation.executionException)}
                </div>
        );
    }
}

