import React, {Component} from "react";
import App, {Section} from './App';
import {Sentence} from "./Sentence";
import {SequenceDiagram} from "./SequenceDiagram";
import {NamedValueTable} from "./NamedValueTable";
import {CapturedInteractions} from "./Interaction";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";

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

export class Invocation extends Component {

    constructor(props) {
        super(props);

        this.state = {
            index: props.index,
            invocation: props.invocation,
            invocationNumber: props.invocationNumber,
            testMethod: props.testMethod,
            isCollapsed: props.isCollapsed,
            stackTracePopupActive: false,
        };

        this.toggle = this.toggle.bind(this);
        this.selectTab = this.selectTab.bind(this);
        this.classForButton = this.classForButton.bind(this);
        this.classForContentBody = this.classForContentBody.bind(this);
        this.isDisabled = this.isDisabled.bind(this);
        this.hideStackTracePopup = this.hideStackTracePopup.bind(this)
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

    failureMessageBlock(executionException) {
        if (executionException["message"]) {
            return (<div key={2} >
                <div className="failure-message">{executionException.message}</div>
                <button className="button is-danger is-small" onClick={() => this.showStackTracePopup()}>Stacktrace...</button>
            </div>)
        }

        return null;
    }

    hasElements(name) {
        let invocationElement = this.state.invocation[name];
        return invocationElement && invocationElement.length > 0;
    }


    buttons(invocation) {
        let highlights = invocation.highlights;

        if (this.hasElements('givens') ||
            this.hasElements('parameters') ||
            this.hasElements('capturedInteractions') ||
            this.hasElements('sequenceDiagram')) {
            return <div>
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

    lazyBody(testBody) {
        if (!this.state.isCollapsed) {
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
                        <span className={"limited-width"}>{invocation.parameterizedTestDescription}</span>
                        <div>
                            <span className={"elapsed-time"}>Elapsed time: {invocation.elapsedTime}</span>
                            <a><FontAwesomeIcon icon={this.icon()}/></a>
                        </div>
                    </div>
                    {this.lazyBody(testBody)}
                </div>
            )
        }

        return null
    }

    showStackTracePopup(e) {
        this.setState({
            stackTracePopupActive: true
        })
    }

    hideStackTracePopup() {
        this.setState({
            stackTracePopupActive: false
        })
    }

    renderStackTracePopup(stackTrace) {
        if (this.state.stackTracePopupActive) {
            return <StackTracePopup onHide={this.hideStackTracePopup} stackTrace={stackTrace}/>
        }
    }

    render() {
        let invocation = this.state.invocation;
        let testStateClass = App.stateClassFor(invocation.state);
        let testBody = <React.Fragment>
            {
                this.props.sectionOrder.map((section) => {
                        switch (section) {
                            case Section.Buttons:
                                return this.buttons(invocation)
                            case Section.Exception:
                                return this.failureMessageBlock(invocation.executionException)
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
            {this.renderStackTracePopup(invocation.executionException.stackTrace)}
        </React.Fragment>
    }
}

