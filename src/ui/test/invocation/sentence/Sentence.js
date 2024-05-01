import React from "react";
import "./sentence.scss"
import Token from "./Token";

const Sentence = ({nested, sentence, acronyms}) =>
    <div className={nested ? "ns" : ""}>
        {
            sentence.map((token, idx) =>
                <Token key={idx} types={token.types} value={token.value} tokens={token.tokens} acronyms={acronyms}/>
            ).reduce((prev, curr) => [prev, " ", curr])
        }
    </div>

export default Sentence