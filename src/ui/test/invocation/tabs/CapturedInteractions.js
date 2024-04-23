import React from "react";
import Interaction from "../Interaction";

const CapturedInteractions = ({capturedInteractions, invocationState, highlights}) => capturedInteractions.map((interaction, index) =>
    <Interaction key={index}
                 invocationState={invocationState}
                 capturedInteraction={interaction}
                 highlights={highlights}/>
);

export default CapturedInteractions