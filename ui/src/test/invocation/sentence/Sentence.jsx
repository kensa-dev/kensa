import React from "react";
import "./Sentence.scss"
import Token from "./Token";

const Sentence = ({sentence}) => {

    const containsNested = sentence.some(token => token.types.includes("tk-ex"))
    const firstIsBlank = sentence[0].types.includes("tk-bl")

    const withSpans = (tokens) => tokens.reduce((prev, curr) => [prev, <span className={"space"}/>, curr])
    const withSpaces = (tokens) => tokens.reduce((prev, curr) => [prev, " ", curr])

    const content = sentence.map((token, idx) =>
        <Token key={idx} token={token}/>
    )

    return (
        <div>
            {firstIsBlank && containsNested ? (
                <>
                    {content[0]}
                    {
                        content.length > 1 && <div className="ns-container">{withSpans(content.slice(2))}</div>
                    }
                </>
            ) : containsNested ? (
                <div className="ns-container">{withSpans(content)}</div>
            ) : (withSpaces(content))}
        </div>
    )
}

export default Sentence