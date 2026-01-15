import React, {useContext, useRef, useState} from "react";
import Sentence from "./Sentence";
import {ConfigContext} from "@/Util";
import './Token.scss';

const Expandable = ({value, tokenCls, parameterTokens, tokens, tokenId}) => {
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
                            ? [<Token key={`${tokenId}-param-${idx}`} token={token}/>]
                            : ['\u00A0', <Token key={`${tokenId}-param-${idx}`} token={token}/>]
                    ))
                )
            )}
            {isExpanded && (
                <div className="ns" style={inlineStyle}>
                    {tokens.map((token, idx) => (
                        token.type === 'table'
                            ? <TokenTable key={`${tokenId}-table-${idx}`} headers={token.headers} rows={token.rows}/>
                            : <Sentence key={`${tokenId}-nested-${idx}`} isNested={true} sentence={token}/>
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
                        token.type === 'table'
                            ? <TokenTable key={`${tokenId}-floating-table-${idx}`} headers={token.headers} rows={token.rows}/>
                            : <Sentence key={`${tokenId}-floating-${idx}`} isNested={true} sentence={token}/>
                    ))}
                </div>
            )}
    </span>
    );
};

const TokenTable = ({headers, rows}) => (
    <table className="tk-table">
        {headers && (
            <thead>
            <tr>
                {headers.map((header, idx) => <th key={`head-${idx}`}>{header}</th>)}
            </tr>
            </thead>
        )}
        <tbody>
        {rows.map((row, rowIdx) => (
            <tr key={`row-${rowIdx}`}>
                {row.map((cell, cellIdx) => (
                    <td key={`cell-${rowIdx}-${cellIdx}`}>
                        <Token token={cell}/>
                    </td>
                ))}
            </tr>
        ))}
        </tbody>
    </table>
);

const AcronymToken = ({tokenCls, value}) => {
    const {acronyms} = useContext(ConfigContext);
    return <span className={"tooltip " + tokenCls} data-tooltip={acronyms[value]}>{value}</span>;
};

const HintedToken = ({tokenCls, value, hint}) => {
    return <span className={"tooltip " + tokenCls} data-tooltip={hint}>{value}</span>;
};

const SimpleToken = ({tokenCls, value}) => <span className={tokenCls}>{value}</span>;

const NoteToken = ({tokenCls, value}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const toggle = () => setIsExpanded(!isExpanded);

    const cls = `${tokenCls} ${isExpanded ? 'is-expanded' : 'is-collapsed'}`;

    return (
        <span
            className={cls}
            data-note={!isExpanded ? value : undefined}
            onClick={toggle}
        >
            {isExpanded ? value : '...'}
            </span>
    );
};

const TextBlockToken = ({tokenCls, value}) => <div className={tokenCls}>{value}</div>;

export const Token = ({token}) => {
    const {types, value, hint, parameterTokens, tokens} = token;
    const tokenCls = types ? types.join(" ") : '';
    const tokenId = token.id || value;

    if (types.includes("tk-ex")) {
        return <Expandable tokenCls={tokenCls} value={value} parameterTokens={parameterTokens} tokens={tokens} tokenId={tokenId}/>;
    } else if (types.includes("tk-ac")) {
        return <AcronymToken tokenCls={tokenCls} value={value}/>;
    } else if (types.includes("tk-tb")) {
        return <TextBlockToken tokenCls={tokenCls} value={value}/>;
    } else if (types.includes("tk-hi")) {
        return <HintedToken tokenCls={tokenCls} value={value} hint={hint}/>;
    } else if (types.includes("tk-nt")) {
        return <NoteToken tokenCls={tokenCls} value={value}/>;
    } else {
        return <SimpleToken tokenCls={tokenCls} value={value}/>;
    }
};

export default Token;