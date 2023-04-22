import React, {Component} from "react";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {RenderableValues} from "./RenderableValues";
import {RenderableAttributes} from "./RenderableAttributes";

class Popup extends Component {
    constructor(props) {
        super(props);

        this.fontSizes = ["font-normal", "font-large"]
        this.state = {
            fontSizeIdx: 0,
        };

        this.onKeyDown = this.onKeyDown.bind(this)
        this.togglePresentationSize = this.togglePresentationSize.bind(this)
    }

    togglePresentationSize() {
        this.setState((prevState) => ({
            fontSizeIdx: prevState.fontSizeIdx === 0 ? 1 : 0
        }))
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
        if (this.props.interaction) {
            let renderedAttributes = this.props.interaction["rendered"]["attributes"]
            let renderedValues = this.props.interaction["rendered"]["values"]

            return (
                <div className={"modal is-active"}>
                    <div className="modal-background" onClick={this.props.onHide}/>
                    <div className="modal-card">
                        <header className="modal-card-head">
                            <p className="modal-card-title">{this.props.interaction.name}</p>
                            <button className={"button is-info is-small mr-5"} onClick={this.togglePresentationSize}>Demo</button>
                            <a onClick={this.props.onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                        </header>
                        <section className={"modal-card-body " + this.fontSizes[this.state.fontSizeIdx]}>
                            <RenderableAttributes highlights={this.props.highlights}
                                                  attributes={renderedAttributes}
                                                  invocationState={this.props.invocationState}
                                                  isSequenceDiagram={true}/>
                            <RenderableValues highlights={this.props.highlights}
                                              values={renderedValues}
                                              invocationState={this.props.invocationState}
                                              interaction={this.state.capturedInteraction}
                                              interactionRef={this.interactionRef}
                                              isSequenceDiagram={true}/>
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
            return <Popup onHide={this.hidePopup}
                          invocationState={this.props.invocationState}
                          interaction={this.state.interaction}
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