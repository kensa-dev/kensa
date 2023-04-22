import React, {Component} from "react";
import Lowlight from "react-lowlight";
import {joinForRegex} from "./Util";
import {highlightJson, highlightPlainText, highlightXml} from "./Highlighting";

export class RenderableValue extends Component {

    constructor(props) {
        super(props)

        let highlightRegexp = props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;

        this.state = {
            highlightRegexp: highlightRegexp,
        }

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

    render() {
        const value = this.props.value;
        const language = value.language ? value.language : "plainText";
        let className = "renderable-value " + this.props.className

        return <div className={className}>
            <div ref={this.interactionRef}>
                <Lowlight className={"scrollable-value"} language={language} value={value.value}/>
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

    classForButton(buttonName) {
        let c = "button ";
        if (this.state.selectedTab === buttonName) {
            c += "is-selected has-background-grey-lighter";
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
            className={this.classForButton(text)}
            onClick={() => this.selectTab(text)}>{text}
        </button>;
    }

    firstNameOrNull() {
        const values = this.props.values;

        if (values.length > 0) {
            let showable = values.filter(this.shouldShow);
            return showable.length > 0 ? showable[0].name : null
        }

        return null
    }

    render() {
        const values = this.props.values;
        const highlights = this.props.highlights;

        return <div>
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