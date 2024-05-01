import React, {useState} from "react";
import Sentence from "./Sentence";

export const Token = ({expanded, types, acronyms, value, tokens}) => {
    const acronymExpansion = () => {
        if (types.includes("tk-ac")) {
            return acronyms[value];
        }
    }

    const classes = () => {
        let c = types.join(" ")

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

        const toggle = () => setIsExpanded(prev => !prev)

        return <>
            <span onClick={toggle} className={classes()} data-tooltip={acronymExpansion()}>{value}</span>
            {
                isExpanded && tokens.map((tokens, idx) =>
                    <Sentence key={idx} nested={true} sentence={tokens} acronyms={acronyms}/>)
            }
        </>
    }

    if (types.includes("tk-ex")) {
        return <NestedSentence/>
    } else {
        return <span className={classes()} data-tooltip={acronymExpansion()}>{value}</span>
    }
}

export default Token