import React from "react";
import "./sentence.scss"
import {Token} from "./Token";

const Sentence = ({nested, acronyms, sentence}) => {
    return (
        <div className={nested ? "ns" : ""}>
            {
                sentence
                    .map((token, index) =>
                        <Token key={index} types={token.types} value={token.value} tokens={token.tokens} expanded={false} acronyms={acronyms}/>
                    )
                    .reduce((prev, curr) => [prev, " ", curr])
            }
        </div>
    )
}

export default Sentence