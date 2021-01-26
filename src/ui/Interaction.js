import React, {Component} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {highlightJson, highlightPlainText, highlightXml} from "./Highlighting";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Lowlight from 'react-lowlight';
import {NamedValueTable} from "./NamedValueTable";
import {joinForRegex} from "./Util";

export class CapturedInteractions extends Component {
    render() {
        const capturedInteractions = this.props.capturedInteractions;
        const highlights = this.props.highlights;
        return (
                <div>
                    {
                        capturedInteractions.map((interaction, index) =>
                                <Interaction key={index} capturedInteraction={interaction} highlights={highlights}/>
                        )
                    }
                </div>
        );
    }
}

export class RenderableAttribute extends Component {
    render() {
        const attribute = this.props.attribute;
        const highlights = this.props.highlights;
        let name = Object.keys(attribute)[1];

        return (
                <div className="box renderable-attribute">
                    <div className="subtitle is-5">{name}</div>
                    <NamedValueTable highlights={highlights} namedValues={attribute[name]}/>
                </div>
        )
    }
}

export class RenderableAttributes extends Component {
    render() {
        const attributes = this.props.attributes;
        const highlights = this.props.highlights;

        return (
                <div>
                    {
                        attributes.map((attribute) => {
                                    return <RenderableAttribute highlights={highlights} attribute={attribute}/>
                                }
                        )
                    }
                </div>
        )
    }
}

export class Interaction extends Component {
    constructor(props) {
        super(props);

        let capturedInteraction = props.capturedInteraction;
        let highlightRegexp = props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;
        let languageAttr = capturedInteraction.attributes.find(attr => attr.hasOwnProperty("language"));
        let language = languageAttr ? languageAttr["language"] : "plainText";

        let renderableAttributes = capturedInteraction["renderableAttributes"];

        this.state = {
            capturedInteraction: capturedInteraction,
            renderableAttributes: renderableAttributes,
            highlightRegexp: highlightRegexp,
            language: language,
            isCollapsed: true
        };

        this.toggle = this.toggle.bind(this);

        this.interactionRef = React.createRef();
    }

    toggle() {
        this.setState(prevState => ({
            isCollapsed: !prevState.isCollapsed
        }));
    }

    contentClass() {
        if (this.state.isCollapsed) {
            return "card-content is-hidden"
        }

        return "card-content";
    }

    icon() {
        if (this.state.isCollapsed) {
            return faAngleDown;
        }

        return faAngleUp;
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
        let capturedInteraction = this.state.capturedInteraction;
        return (
            <div className="captured-interaction card is-fullwidth">
                <header className="card-header" onClick={this.toggle}>
                    <p className="card-header-title">{capturedInteraction.name}</p>
                    <a className="card-header-icon">
                        <FontAwesomeIcon icon={this.icon()}/>
                    </a>
                </header>
                <div className={this.contentClass()}>
                    <RenderableAttributes highlights={this.props.highlights}
                                          attributes={this.state.renderableAttributes}/>
                    <div ref={this.interactionRef} className="box">
                        <Lowlight language={this.state.language} value={capturedInteraction['value']}/>
                    </div>
                </div>
            </div>
        )
    }
}