import React, {useState} from "react";
import {CollapseIcon} from "../../../Util";
import InteractionContent from "./interaction/InteractionContent";

const Interaction = ({capturedInteraction, highlights}) => {
    const [isCollapsed, setIsCollapsed] = useState(true);

    const toggle = () => setIsCollapsed(!isCollapsed)

    return (
        <div className="card is-fullwidth">
            <header className="card-header" onClick={toggle}>
                <p className="card-header-title">{capturedInteraction.name}</p>
                <a className="card-header-icon"><CollapseIcon isCollapsed={isCollapsed}/></a>
            </header>
            {isCollapsed ||
                <div className={"card-content"}>
                    <InteractionContent interaction={capturedInteraction["rendered"]} highlights={highlights}/>
                </div>
            }
        </div>
    )
};

const CapturedInteractions = ({capturedInteractions, highlights}) =>
    capturedInteractions.map((interaction, index) =>
        <Interaction key={index}
                     capturedInteraction={interaction}
                     highlights={highlights}/>
    );

export default CapturedInteractions