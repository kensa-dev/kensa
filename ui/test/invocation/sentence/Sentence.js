import React from "react";
import "./sentence.scss"
import Token from "./Token";

const Sentence = ({nested, sentence}) =>
    <div className={nested ? "ns" : ""}>
        {
            sentence.map((token, idx) =>
                <Token key={idx} types={token.types} value={token.value} tokens={token.tokens}/>
            ).reduce((prev, curr) => [prev, " ", curr])
        }
    </div>

export default Sentence