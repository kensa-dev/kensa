import React, {Component} from "react";
import Lowlight from 'react-lowlight';
import {highlightJson, highlightXml} from "./Highlighting";

class Popup extends Component {
    constructor(props) {
        super(props);
        this.lowlightRef = React.createRef();

        this.state = {
            language: 'plainText'
        }
    }

    componentDidUpdate() {
        if (this.props.interaction) {
            let languageElement = this.props.interaction.attributes.find(element => {
                return element['name'] === 'language'
            });
            let language = languageElement ? languageElement['value'] : 'plainText';

            if (this.state.language !== language) {
                this.setState({language: language})
            }

            let highlightRegexp = this.props.highlights.length > 0 ? new RegExp(`(${this.props.highlights.join('|')})`) : null;
            if (highlightRegexp) {
                let codeNode = this.lowlightRef.current.children[0].firstChild;
                if (this.state.language === 'xml') {
                    highlightXml(codeNode, highlightRegexp);
                } else if (this.state.language === 'json') {
                    highlightJson(codeNode, highlightRegexp);
                }
            }
        }
    }

    render() {
        if (this.props.interaction) {
            return (
                    <div className={"modal is-active"}>
                        <div className="modal-background" onClick={this.props.hide}/>
                        <div className="modal-card">
                            <header className="modal-card-head">
                                <p className="modal-card-title">{this.props.interaction.name}</p>
                            </header>
                            <section className="modal-card-body">
                                <div ref={this.lowlightRef}>
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
            modalVisible: false,
            interaction: null
        };

        this.onClick = this.onClick.bind(this)
        this.hideModal = this.hideModal.bind(this)
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
        let interactionName = target.getAttribute("sequence_diagram_interaction_name").replace(/_/g, " ");

        return this.props.capturedInteractions.find(interaction => {
            return interaction.name === interactionName;
        });
    }

    onClick(e) {
        let interaction = this.findInteractionFor(e.target);

        this.setState({
            modalVisible: true,
            interaction: interaction
        })
    }

    hideModal() {
        this.setState({
            modalVisible: false,
            interaction: null
        })
    }

    render() {
        const sequenceDiagram = this.props.sequenceDiagram;
        return (
                <div>
                    <div ref={this.sequenceDiagramRef} dangerouslySetInnerHTML={{__html: sequenceDiagram}}/>
                    <Popup hide={this.hideModal} interaction={this.state.interaction} highlights={this.props.highlights}/>
                </div>
        )
    }
}