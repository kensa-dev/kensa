import React, {useState} from "react";
import Sentence from "./Sentence";

const NestedSentence = ({value, tokenCls, tokens, acronyms}) => {
    const [isExpanded, setIsExpanded] = useState(false)

    const toggle = () => setIsExpanded(prev => !prev)

    return <>
        <span onClick={toggle} className={tokenCls}>{value}</span>
        {
            isExpanded && tokens.map((tokens, idx) =>
                <Sentence key={idx} nested={true} sentence={tokens} acronyms={acronyms}/>)
        }
    </>
}

const AcronymToken = ({tokenCls, acronym, value}) =>
    <span className={"tooltip " + tokenCls} data-tooltip={acronym}>{value}</span>

export const Token = ({types, acronyms, value, tokens}) => {
    const tokenCls = types.join(" ")

    if (types.includes("tk-ex")) {
        return <NestedSentence tokenCls={tokenCls} value={value} tokens={tokens} acronyms={acronyms}/>
    } else if (types.includes("tk-ac")) {
        return <AcronymToken tokenCls={tokenCls} acronym={acronyms[value]} value={value}/>
    } else {
        return <span className={tokenCls}>{value}</span>
    }
}

export default Token