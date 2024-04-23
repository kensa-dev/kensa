import RenderedAttributes from "./RenderedAttributes";
import RenderedValues from "./RenderedValues";
import React from "react";

const InteractionContent = ({interaction, highlights}) =>
    <>
        <RenderedAttributes highlights={highlights} attributes={interaction["attributes"]}/>
        <RenderedValues highlights={highlights} values={interaction["values"]}/>
    </>

export default InteractionContent