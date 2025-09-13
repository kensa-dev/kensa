import React, {useContext, useRef, useState} from "react";
import Sentence from "./Sentence";
import {ConfigContext} from "@/Util";
import './Token.scss';

const NestedSentence = ({value, tokenCls, parameterTokens, tokens, tokenId}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const [isFloated, setIsFloated] = useState(false);
    const tkExRef = useRef(null);
    const leaveTimeoutRef = useRef(null);
    const timeoutRef = useRef(null);
    const [inlineStyle, setInlineStyle] = useState({});

    const toggle = () => {
        if (tkExRef.current) {
            const tkExRect = tkExRef.current.getBoundingClientRect();
            const parentRect = tkExRef.current.closest('.ns-container')?.getBoundingClientRect() || document.body.getBoundingClientRect();
            setInlineStyle({
                position: 'relative',
                left: `${tkExRect.left - parentRect.left}px`,
                marginTop: `1px`,
            });
        }
        setIsExpanded(prev => {
            const newExpanded = !prev;
            if (newExpanded) {
                setIsFloated(false);
                clearTimeout(timeoutRef.current);
                clearTimeout(leaveTimeoutRef.current);
            }
            return newExpanded;
        });
    };

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

    React.useEffect(() => {
        return () => {
            clearTimeout(timeoutRef.current);
            clearTimeout(leaveTimeoutRef.current);
        };
    }, []);

    return (
        <span className="ns-wrapper">
      <span
          ref={tkExRef}
          onClick={toggle}
          onMouseEnter={handleMouseEnter}
          onMouseLeave={handleMouseLeave}
          className={tokenCls}
      >
        {value}
      </span>{" "}
            {parameterTokens && parameterTokens.length > 0 && (
                React.createElement(
                    parameterTokens[0].types.includes('tk-nl') ? 'div' : 'span',
                    {className: "ns-parameters"},
                    parameterTokens.flatMap((token, idx) => (
                        idx === 0
                            ? [<Token key={`${tokenId}-param-${idx}`} token={token} />]
                            : ['\u00A0', <Token key={`${tokenId}-param-${idx}`} token={token} />]
                    ))
                )
            )}
            {isExpanded && (
                <div className="ns" style={inlineStyle}>
                    {tokens.map((token, idx) => (
                        <Sentence key={`${tokenId}-nested-${idx}`} isNested={true} sentence={token}/>
                    ))}
                </div>
            )}
            {isFloated && (
                <div
                    className="ns-floating"
                    onMouseEnter={handleMouseEnter}
                    onMouseLeave={handleMouseLeave}
                >
                    {tokens.map((token, idx) => (
                        <Sentence key={`${tokenId}-floating-${idx}`} isNested={true} sentence={token}/>
                    ))}
                </div>
            )}
    </span>
    );
};

const AcronymToken = ({tokenCls, value}) => {
    const {acronyms} = useContext(ConfigContext);
    return <span className={"tooltip " + tokenCls} data-tooltip={acronyms[value]}>{value}</span>;
};

const SimpleToken = ({tokenCls, value}) => <span className={tokenCls}>{value}</span>;

const TextBlockToken = ({tokenCls, value}) => <div className={tokenCls}>{value}</div>;

export const Token = ({token}) => {
    const {types, value, parameterTokens, tokens} = token;
    const tokenCls = types ? types.join(" ") : '';
    const tokenId = token.id || value; // Use token.id if available, else value

    if (types.includes("tk-ex")) {
        return <NestedSentence tokenCls={tokenCls} value={value} parameterTokens={parameterTokens} tokens={tokens} tokenId={tokenId}/>;
    } else if (types.includes("tk-ac")) {
        return <AcronymToken tokenCls={tokenCls} value={value}/>;
    } else if (types.includes("tk-tb")) {
        return <TextBlockToken tokenCls={tokenCls} value={value}/>;
    } else {
        return <SimpleToken tokenCls={tokenCls} value={value}/>;
    }
};

export default Token;