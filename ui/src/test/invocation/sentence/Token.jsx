import React, {useContext, useEffect, useRef, useState} from "react";
import Sentence from "./Sentence";
import {ConfigContext} from "@/Util";
import './Token.scss'

const NestedSentence = ({value, tokenCls, parameterTokens, tokens}) => {
    const [isExpanded, setIsExpanded] = useState(false)
    const [isFloated, setIsFloated] = useState(false)
    const leaveTimeoutRef = useRef(null);
    const timeoutRef = useRef(null);

    const toggle = () => setIsExpanded(prev => !prev)
    useEffect(() => {
        clearTimeout(timeoutRef.current);
        clearTimeout(leaveTimeoutRef.current);
        if (isExpanded) {
            setIsFloated(false);
        }
    }, [isExpanded]);

    const handleMouseEnter = () => {
        if (isExpanded) return;
        clearTimeout(leaveTimeoutRef.current);
        timeoutRef.current = setTimeout(() => {
            setIsFloated(true);
        }, 500);
    };

    const handleMouseLeave = () => {
        if (isExpanded) return;
        clearTimeout(timeoutRef.current);
        leaveTimeoutRef.current = setTimeout(() => {
            setIsFloated(false);
        }, 100);
    };

    useEffect(() => {
        return () => {
            clearTimeout(timeoutRef.current);
            clearTimeout(leaveTimeoutRef.current);
        };
    }, []);

    return <>
        <span className="ns-wrapper">
            <span onClick={toggle} onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave} className={tokenCls}>{value}</span>{" "}
            {
                parameterTokens && parameterTokens.length > 0 && (
                    React.createElement(
                        parameterTokens[0].types.includes('tk-nl') ? 'div' : 'span',
                        { className: "ns-parameters" },
                        parameterTokens.map((parameterToken, idx) =>
                            <Token key={idx} token={parameterToken}/>
                        ).reduce((prev, curr) => [prev, " ", curr])
                    )
                )
            }
            {
                isExpanded && (
                    <div className="ns">
                        {tokens.map((token, idx) =>
                            <Sentence key={idx} isNested={true} sentence={token} />
                        )}
                    </div>
                )
            }
            {
                isFloated &&
                <div className="ns-floating"
                     onMouseEnter={handleMouseEnter}
                     onMouseLeave={handleMouseLeave}
                >
                    {tokens.map((token, idx) => (
                        <Sentence key={idx} sentence={token}/>
                    ))}
                </div>
            }
            </span>
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

export const Token = ({token}) => {
    const {types, value, parameterTokens, tokens} = token
    const tokenCls = types ? types.join(" ") : ''

    if (types.includes("tk-ex")) {
        return <NestedSentence tokenCls={tokenCls} value={value} parameterTokens={parameterTokens} tokens={tokens}/>
    } else if (types.includes("tk-ac")) {
        return <AcronymToken tokenCls={tokenCls} value={value}/>
    } else if (types.includes("tk-tb")) {
        return <TextBlockToken tokenCls={tokenCls} value={value}/>
    } else {
        return <SimpleToken tokenCls={tokenCls} value={value}/>
    }
}

export default Token