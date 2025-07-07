import React from "react";
import "./Sentence.scss"
import Token from "./Token";

const Sentence = ({isNested, sentence}) =>
    <div className={isNested ? "ns" : ""}>
        {
            sentence.map((token, idx) =>
                <Token key={idx} token={token}/>
            ).reduce((prev, curr) => [prev, " ", curr])
        }
    </div>

export default Sentence