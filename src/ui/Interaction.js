import React, {Component} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {RenderableValues} from "./RenderableValues";
import {RenderableAttributes} from "./RenderableAttributes";

export class CapturedInteractions extends Component {
    render() {
        return (
            <div>
                {
                    this.props.capturedInteractions.map((interaction, index) =>
                        <Interaction key={index}
                                     invocationState={this.props.invocationState}
                                     capturedInteraction={interaction}
                                     highlights={this.props.highlights}/>
                    )
                }
            </div>
        );
    }
}

export class Interaction extends Component {
    constructor(props) {
        super(props);

        let invocationState = props.invocationState;
        let capturedInteraction = props.capturedInteraction;

        let renderedAttributes = capturedInteraction["rendered"]["attributes"];
        let renderedValues = capturedInteraction["rendered"]["values"];

        this.fontSizes = ["font-normal", "font-large"]

        this.state = {
            invocationState: invocationState,
            capturedInteraction: capturedInteraction,
            renderedAttributes: renderedAttributes,
            renderedValues: renderedValues,
            isCollapsed: true,
            selectedTab: null,
            selectedValueTab: null,
            fontSizeIdx: 0
        };

        this.toggle = this.toggle.bind(this);
        this.togglePresentationSize = this.togglePresentationSize.bind(this)
    }

    togglePresentationSize(e) {
        e.stopPropagation()
        this.setState((prevState) => ({
            fontSizeIdx: prevState.fontSizeIdx === 0 ? 1 : 0
        }))
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

        return "card-content " + this.fontSizes[this.state.fontSizeIdx];
    }

    demoButtonClass() {
        let cls = "card-header-icon button is-info is-small"
        if(this.state.isCollapsed) {
            cls = cls + " is-hidden"
        }

        return cls
    }

    icon() {
        if (this.state.isCollapsed) {
            return faAngleDown;
        }

        return faAngleUp;
    }

    render() {
        let capturedInteraction = this.state.capturedInteraction;

        return (
            <div className="captured-interaction card is-fullwidth">
                <header className="card-header" onClick={this.toggle}>
                    <p className="card-header-title">{capturedInteraction.name}</p>
                    <button className={this.demoButtonClass()} onClick={this.togglePresentationSize}>Demo</button>
                    <a className="card-header-icon"><FontAwesomeIcon icon={this.icon()}/></a>
                </header>
                <div className={this.contentClass()}>
                    <RenderableAttributes highlights={this.props.highlights}
                                          attributes={this.state.renderedAttributes}
                                          invocationState={this.state.invocationState}/>
                    <RenderableValues highlights={this.props.highlights}
                                      values={this.state.renderedValues}
                                      invocationState={this.state.invocationState}
                                      interaction={this.state.capturedInteraction}
                                      interactionRef={this.interactionRef}/>
                </div>
            </div>
        )
    }
}