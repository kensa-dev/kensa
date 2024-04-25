import React, {useState} from "react";
import Sentence from "./Sentence";

export const Token = ({expanded, types, acronyms, value, tokens}) => {
    const acronymExpansion = () => {
        if (types.includes("tk-ac")) {
            return acronyms[value];
        }
    }

    const classes = () => {
        let c = ""

        types.forEach(type => {
            if (c.length > 0) c += " "
            c += type;
        })

        if (types.includes("tk-ac")) {
            c = "tooltip " + c;
        }

        if (types.includes("tk-ex") && expanded) {
            c += " expanded"
        }

        return c;
    }

    const NestedSentence = () => {
        const [isExpanded, setIsExpanded] = useState(expanded)

        return <>
            <span onClick={() => setIsExpanded(prev => !prev)} className={classes()} data-tooltip={acronymExpansion()}>{value}</span>
            {isExpanded ?
                tokens.map((tokens, index) =>
                    <Sentence nested={true} sentence={tokens} acronyms={acronyms} key={index}/>)
                : null
            }
        </>
    }

    if (types.includes("tk-ex")) {
        return <NestedSentence/>
    } else {
        return <span className={classes()} data-tooltip={acronymExpansion()}>{value}</span>
    }
}