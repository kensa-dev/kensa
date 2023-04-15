import React, {Component} from "react";
import App from "./App";
import {NamedValueTable} from "./NamedValueTable";

export class RenderableAttribute extends Component {
    render() {
        const attribute = this.props.attribute;
        const highlights = this.props.highlights;
        let className = "renderable-attribute font-small " + this.props.className

        return (
            <div className={className}>
                <NamedValueTable highlights={highlights} namedValues={attribute.attributes}/>
            </div>
        )
    }
}

export class RenderableAttributes extends Component {

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

    buttonForAttribute(text, idx) {
        return <button
            key={idx}
            className={this.classForButton(text, App.stateClassFor(this.props.invocationState))}
            onClick={() => this.selectTab(text)}>{text}
        </button>;
    }

    firstNameOrNull() {
        const attributes = this.props.attributes

        if (attributes.length > 0) {
            return attributes.filter(this.shouldShow)[0].name
        }

        return null
    }

    render() {
        const attributes = this.props.attributes;
        const highlights = this.props.highlights;

        return <div>
            <div className="buttons has-addons are-small">
                <p className="control">
                    {
                        attributes
                            .filter(this.shouldShow)
                            .map((attribute, idx) => {
                                return this.buttonForAttribute(attribute.name, idx)
                            })
                    }
                </p>
            </div>
            {
                attributes
                    .filter(this.shouldShow)
                    .map((attribute, idx) =>
                        <RenderableAttribute key={idx}
                                             className={this.classForBody(attribute.name)}
                                             highlights={highlights}
                                             attribute={attribute}/>)
            }
        </div>
    }
}