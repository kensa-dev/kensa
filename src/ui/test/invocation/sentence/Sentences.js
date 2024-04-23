import React from "react";
import Sentence from "./Sentence";

const Sentences = ({invocation}) => {
    return (
        <div className="sentences">
            {invocation.sentences.map((sentence, index) => <Sentence key={index}
                                                                     expanded={false}
                                                                     sentence={sentence}
                                                                     acronyms={invocation.acronyms}/>)}
        </div>
    )
}

export default Sentences