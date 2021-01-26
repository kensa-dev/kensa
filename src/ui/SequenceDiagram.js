import React, {Component} from "react";
import Lowlight from 'react-lowlight';
import {highlightJson, highlightPlainText, highlightXml} from "./Highlighting";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {joinForRegex} from "./Util";

class RenderableAttributes extends Component {

    setCodeRef(wrappingDiv) {
        if (wrappingDiv) {
            let highlightRegexp = this.props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;
            if (highlightRegexp) {
                highlightPlainText(wrappingDiv, highlightRegexp)
            }
        }
    }

    render() {
        const attributes = this.props.attributes;
        return (
            <div>
                <table className="table pairs is-borderless is-narrow">
                    <tbody>
                    {attributes
                        .map((attribute) => {
                                if (attribute["showOnSequenceDiagram"]) {
                                    let attributeName = Object.keys(attribute)[1];
                                    return attribute[attributeName].map((item, index) => {
                                        let itemName = Object.keys(item)[0];
                                        let value = item[itemName];
                                        return (<tr key={index}>
                                            <td  className="name">{itemName}</td>
                                            <td>
                                                <div ref={this.setCodeRef.bind(this)}>{value}</div>
                                            </td>
                                        </tr>);
                                    })

                                }
                            }
                        )}
                    </tbody>
                </table>
                {attributes.length > 0 ? <div className="is-divider"/> : null}
            </div>
        );
    }
}

class Popup extends Component {
    constructor(props) {
        super(props);

        this.state = {
            language: this.deriveLanguage()
        };

        this.onKeyDown = this.onKeyDown.bind(this)
    }

    deriveLanguage() {
        let languageAttr;
        if (this.props.interaction) {
            languageAttr = this.props.interaction.attributes.find(attr => attr.hasOwnProperty("language"));
        }
        return languageAttr ? languageAttr["language"] : "plainText";
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

    setCodeRef(wrappingDiv) {
        if (wrappingDiv && this.props.interaction) {
            let highlightRegexp = this.props.highlights.length > 0 ? new RegExp(`(${joinForRegex(this.props.highlights)})`) : null;
            if (highlightRegexp) {
                let codeNode = wrappingDiv.firstElementChild.firstElementChild;
                if (this.state.language === 'xml') {
                    highlightXml(codeNode, highlightRegexp);
                } else if (this.state.language === 'json') {
                    highlightJson(codeNode, highlightRegexp);
                } else {
                    highlightPlainText(codeNode, highlightRegexp)
                }
            }
        }
    }

    render() {
        if (this.props.interaction) {
            return (
                <div className={"modal is-active"}>
                    <div className="modal-background" onClick={this.props.onHide}/>
                    <div className="modal-card">
                        <header className="modal-card-head">
                            <p className="modal-card-title">{this.props.interaction.name}</p>
                            <a onClick={this.props.onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                        </header>
                        <section className="modal-card-body">
                            <RenderableAttributes highlights={this.props.highlights}
                                                                 attributes={this.props.interaction.renderableAttributes}/>
                            <div ref={this.setCodeRef.bind(this)}>
                                <Lowlight language={this.state.language} value={this.props.interaction.value}/>
                            </div>
                        </section>
                    </div>
                </div>
            )
        }
        return null;
    }
}

export class SequenceDiagram extends Component {

    constructor(props) {
        super(props);

        this.sequenceDiagramRef = React.createRef();
        this.clickableNodes = [];

        this.state = {
            popupActive: false,
            interaction: null
        };

        this.onClick = this.onClick.bind(this);
        this.hidePopup = this.hidePopup.bind(this)
    }

    findClickableChildrenOf(parent, clickable) {
        Array.from(parent.childNodes).forEach(child => {
                if (child.classList && child.classList.contains('sequence_diagram_clickable')) {
                    clickable.push(child);
                }
                this.findClickableChildrenOf(child, clickable);
            }
        );
    }

    componentDidMount() {
        let svgNode = this.sequenceDiagramRef.current.firstElementChild;
        this.findClickableChildrenOf(svgNode, this.clickableNodes);

        this.clickableNodes.forEach(node => {
            node.addEventListener('click', this.onClick);
        })
    }

    componentWillUnmount() {
        this.clickableNodes.forEach(node => {
            node.removeEventListener('click', this.onClick);
        })
    }

    findInteractionFor(target) {
        let interactionId = target.getAttribute("sequence_diagram_interaction_id");

        return this.props.capturedInteractions.find(interaction => {
            return interaction.id === interactionId;
        });
    }

    onClick(e) {
        let interaction = this.findInteractionFor(e.target);

        this.setState({
            popupActive: true,
            interaction: interaction
        })
    }

    hidePopup() {
        this.setState({
            popupActive: false,
            interaction: null
        })
    }

    popup() {
        if (this.state.popupActive) {
            return <Popup onHide={this.hidePopup} interaction={this.state.interaction}
                          highlights={this.props.highlights}/>
        }
    }

    render() {
        const sequenceDiagram = this.props.sequenceDiagram;
        return (
            <div>
                <div ref={this.sequenceDiagramRef} dangerouslySetInnerHTML={{__html: sequenceDiagram}}/>
                {this.popup()}
            </div>
        )
    }
}