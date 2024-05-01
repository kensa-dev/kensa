import React from "react";
import Sentence from "./Sentence";

const Sentences = ({invocation}) =>
    <div className="sentences">
        {
            invocation.sentences.map((sentence, idx) =>
                <Sentence key={idx} expanded={false} sentence={sentence} acronyms={invocation.acronyms}/>)
        }
    </div>

export default Sentences