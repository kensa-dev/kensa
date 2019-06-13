import React, {Component} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {highlightJson, highlightXml} from "./Highlighting";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Lowlight from 'react-lowlight';
import {NamedValueTable} from "./NamedValueTable";

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

export class Renderable extends Component {
    render() {
        const renderable = this.props.renderable;
        const highlights = this.props.highlights;

        return (
                <div className="box renderable">
                    <div className="subtitle is-5">{renderable['name']}</div>
                    <NamedValueTable highlights={highlights} namedValues={renderable['value']}/>
                </div>
        )
    }
}

export class Renderables extends Component {
    render() {
        const renderables = this.props.renderables;
        const highlights = this.props.highlights;

        return (
                <div>
                    {
                        renderables.map((renderable) => {
                                    if (renderable['name'] !== 'value') {
                                        return <Renderable highlights={highlights} renderable={renderable}/>
                                    }
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
        let highlightRegexp = props.highlights.length > 0 ? new RegExp(`(${props.highlights.join('|')})`) : null;
        let languageElement = capturedInteraction.attributes.find(element => {
            return element['name'] === 'language'
        });
        let language = languageElement ? languageElement['value'] : 'plainText';

        let renderables = capturedInteraction['renderables'];

        this.state = {
            capturedInteraction: capturedInteraction,
            renderables: renderables,
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
                        <Renderables highlights={this.props.highlights} renderables={this.state.renderables}/>
                        <div ref={this.interactionRef} className="box">
                            <Lowlight language={this.state.language} value={capturedInteraction['value']}/>
                        </div>
                    </div>
                </div>
        )
    }
}