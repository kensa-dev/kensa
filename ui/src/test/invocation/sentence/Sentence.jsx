import React from "react";
import "./Sentence.scss"
import Token from "./Token";

const Sentence = ({sentence}) =>
    <div>
        {
            sentence.map((token, idx) =>
                <Token key={idx} token={token}/>
            ).reduce((prev, curr) => [prev, " ", curr])
        }
    </div>

export default Sentence