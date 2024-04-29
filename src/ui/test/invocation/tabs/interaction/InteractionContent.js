import RenderedAttributes from "./RenderedAttributes";
import RenderedValues from "./RenderedValues";
import React from "react";

const InteractionContent = ({interaction, highlights, isSequenceDiagram}) =>
    <>
        <RenderedAttributes highlights={highlights}
                            attributes={interaction["attributes"]}
                            isSequenceDiagram={isSequenceDiagram}/>
        <RenderedValues highlights={highlights}
                        values={interaction["values"]}
                        isSequenceDiagram={isSequenceDiagram}/>
    </>

export default InteractionContent