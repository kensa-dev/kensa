import React from "react";
import "./Sentence.scss"
import Token from "./Token";

const Sentence = ({sentence}) => {

    const containsNested = sentence.some(token => token.types.includes("tk-ex"))
    const firstIsBlank = sentence[0].types.includes("tk-bl")

    const withSpaces = (tokens) => tokens.flatMap((token, index) => {
        if (index === 1 && tokens[0].props.token.types.includes("tk-bl")) {
            return [token];
        }
        return index === 0 ? [token] : ['\u00A0', token];
    });

    const content = sentence.map((token, idx) =>
        <Token key={idx} token={token}/>
    )

    return (
        <div>
            {firstIsBlank && containsNested ? (
                <>
                    {content[0]}
                    {
                        content.length > 1 && <div className="ns-container">{withSpaces(content.slice(1))}</div>
                    }
                </>
            ) : containsNested ? (
                <div className="ns-container">{withSpaces(content)}</div>
            ) : (withSpaces(content))}
        </div>
    )
}

export default Sentence