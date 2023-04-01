import React, {Component} from "react";
import App from "./App";
import Lowlight from "react-lowlight";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {joinForRegex} from "./Util";
import {highlightJson, highlightPlainText, highlightXml} from "./Highlighting";

export class RenderableValue extends Component {

    constructor(props) {
        super(props)

        this.fontSizes = ["font-small", "font-normal", "font-medium", "font-large"]
        let highlightRegexp = props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;

        this.state = {
            fontSizeIdx: 0,
            highlightRegexp: highlightRegexp,
        }

        this.grow = this.grow.bind(this)
        this.shrink = this.shrink.bind(this)

        this.interactionRef = React.createRef();
    }

    componentDidMount() {
        let highlightRegexp = this.state.highlightRegexp;
        if (highlightRegexp) {
            let codeNode = this.interactionRef.current.children[0].firstChild
            if (this.state.language === 'xml') {
                highlightXml(codeNode, highlightRegexp);
            } else if (this.state.language === 'json') {
                highlightJson(codeNode, highlightRegexp);
            } else {
                highlightPlainText(codeNode, highlightRegexp);
            }
        }
    }

    grow() {
        this.setState(prevState => ({
            fontSizeIdx: Math.min(prevState.fontSizeIdx + 1, 3)
        }));
    }

    shrink() {
        this.setState(prevState => ({
            fontSizeIdx: Math.max(prevState.fontSizeIdx - 1, 0)
        }));
    }

    render() {
        const value = this.props.value;
        const language = value.language ? value.language : "plainText";
        let className = "renderable-value " + this.props.className

        return <div className={className}>
            <div className="buttons has-addons">
                <p className="control">
                    <button className="button is-tiny" onClick={this.grow}>
                        <span className="icon"><FontAwesomeIcon icon={faPlus}/></span>
                    </button>
                    <button className="button is-tiny" onClick={this.shrink}>
                        <span className="icon"><FontAwesomeIcon icon={faMinus}/></span>
                    </button>
                </p>
            </div>
            <div ref={this.interactionRef} className={this.fontSizes[this.state.fontSizeIdx]}>
                <Lowlight language={language} value={value.value}/>
            </div>
        </div>
    }
}

export class RenderableValues extends Component {


    constructor(props) {
        super(props)

        this.state = {
            selectedTab: null
        }

        this.shouldShow = (attribute) => this.props.isSequenceDiagram && attribute.showOnSequenceDiagram || !this.props.isSequenceDiagram

    }

    componentDidMount() {
        this.selectTab(this.firstNameOrNull())
    }

    selectTab(tabName) {
        this.setState({selectedTab: tabName});
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

    classForBody(name) {
        if (this.state.selectedTab === name) {
            return "";
        }

        return "is-hidden"
    }

    buttonFor(text, idx) {
        return <button
            key={idx}
            className={this.classForButton(text, App.stateClassFor(this.props.invocationState))}
            onClick={() => this.selectTab(text)}>{text}
        </button>;
    }

    firstNameOrNull() {
        const values = this.props.values;

        if (values.length > 0) {
            return values.filter(this.shouldShow)[0].name
        }

        return null
    }

    render() {
        const values = this.props.values;
        const highlights = this.props.highlights;

        return <div className="is-small">
            <div className="buttons has-addons are-small">
                <p className="control">
                    {
                        values
                            .filter(this.shouldShow)
                            .map((value, idx) => this.buttonFor(value.name, idx))
                    }
                </p>
            </div>
            {
                values
                    .filter(this.shouldShow)
                    .map((value, idx) => {
                        return <RenderableValue key={idx}
                                                className={this.classForBody(value.name)}
                                                value={value}
                                                highlights={highlights}
                                                interactionRef={this.props.interactionRef}/>;
                    })
            }
        </div>
    }
}