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

        this.state = {
            invocationState: invocationState,
            capturedInteraction: capturedInteraction,
            renderedAttributes: renderedAttributes,
            renderedValues: renderedValues,
            isCollapsed: true,
            selectedTab: null,
            selectedValueTab: null
        };

        this.toggle = this.toggle.bind(this);
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