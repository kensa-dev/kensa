import React, {useContext, useState} from "react";
import Sentence from "./Sentence";
import {ConfigContext} from "@/Util";
import './Token.scss'

const NestedSentence = ({value, tokenCls, tokens}) => {
    const [isExpanded, setIsExpanded] = useState(false)

    const toggle = () => setIsExpanded(prev => !prev)

    return <>
        <span onClick={toggle} className={tokenCls}>{value}</span>
        {
            isExpanded && tokens.map((tokens, idx) =>
                <Sentence key={idx} nested={true} sentence={tokens}/>)
        }
    </>
}

const AcronymToken = ({tokenCls, value}) => {
    const {acronyms} = useContext(ConfigContext)
    return <span className={"tooltip " + tokenCls} data-tooltip={acronyms[value]}>{value}</span>;
}

const SimpleToken = ({tokenCls, value}) =>
    <span className={tokenCls}>{value}</span>

const TextBlockToken = ({tokenCls, value}) =>
    <div className={tokenCls}>{value}</div>

export const Token = ({types, value, tokens}) => {
    const tokenCls = types.join(" ")

    if (types.includes("tk-ex")) {
        return <NestedSentence tokenCls={tokenCls} value={value} tokens={tokens}/>
    } else if (types.includes("tk-ac")) {
        return <AcronymToken tokenCls={tokenCls} value={value}/>
    } else if (types.includes("tk-tb")) {
        return <TextBlockToken tokenCls={tokenCls} value={value}/>
    } else {
        return <SimpleToken tokenCls={tokenCls} value={value}/>
    }
}

export default Token