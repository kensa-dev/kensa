import React, {useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {RenderableValues} from "../RenderableValues";
import {RenderableAttributes} from "../RenderableAttributes";
import {collapseIcon} from "../../Util";

const Interaction = ({invocationState, capturedInteraction, highlights}) => {
    const renderedAttributes = capturedInteraction["rendered"]["attributes"];
    const renderedValues = capturedInteraction["rendered"]["values"];
    const fontSizes = ["font-normal", "font-large"]
    const [fontSizeIdx, setFontSizeIdx] = useState(0);
    const [isCollapsed, setIsCollapsed] = useState(true);

    const togglePresentationSize = (event) => {
        event.stopPropagation()
        setFontSizeIdx(fontSizeIdx === 0 ? 1 : 0)
    }

    const toggle = () => {
        setIsCollapsed(!isCollapsed)
    }

    const contentClass = () => {
        if (isCollapsed) {
            return "card-content is-hidden"
        }

        return "card-content " + fontSizes[fontSizeIdx];
    }

    const demoButtonClass = () => {
        let cls = "card-header-icon button is-info is-small"
        if (isCollapsed) {
            cls = cls + " is-hidden"
        }

        return cls
    }

    return (
        <div className="captured-interaction card is-fullwidth">
            <header className="card-header" onClick={toggle}>
                <p className="card-header-title">{capturedInteraction.name}</p>
                <button className={demoButtonClass()} onClick={togglePresentationSize}>Demo</button>
                <a className="card-header-icon"><FontAwesomeIcon icon={collapseIcon(isCollapsed)}/></a>
            </header>
            <div className={contentClass()}>
                <RenderableAttributes highlights={highlights}
                                      attributes={renderedAttributes}
                                      invocationState={invocationState}/>
                <RenderableValues highlights={highlights}
                                  values={renderedValues}
                                  invocationState={invocationState}
                                  interaction={capturedInteraction}/>
            </div>
        </div>
    )
};

export default Interaction