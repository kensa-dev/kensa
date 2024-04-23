import React, {Component, createRef, useEffect, useState} from "react";
import Lowlight from "react-lowlight";
import {joinForRegex} from "../Util";
import {highlightJson, highlightPlainText, highlightXml} from "./Highlighting";

export const RenderableValue = ({highlights, value, className}) => {
    const highlightRegexp = highlights.length > 0 ? new RegExp(`(${joinForRegex(highlights)})`) : null;
    const interactionRef = createRef();
    const language = value.language ? value.language : "plainText";

    useEffect(() => {
        if (highlightRegexp) {
            let codeNode = interactionRef.current.children[0].firstChild
            if (language === 'xml') {
                highlightXml(codeNode, highlightRegexp);
            } else if (language === 'json') {
                highlightJson(codeNode, highlightRegexp);
            } else {
                highlightPlainText(codeNode, highlightRegexp);
            }
        }
    }, []);

    return <div className={"renderable-value " + className}>
        <div ref={interactionRef}>
            <Lowlight className={"scrollable-value"} language={language} value={value.value}/>
        </div>
    </div>
}

export const RenderableValues = ({isSequenceDiagram, values, highlights}) => {
    const useTabs = values.length > 1
    const shouldShow = (attribute) => (isSequenceDiagram && attribute.showOnSequenceDiagram) || !isSequenceDiagram
    const [selectedTab, setSelectedTab] = useState(null);

    const firstNameOrNull = () => {
        if (values.length > 0) {
            let showable = values.filter(shouldShow);
            return showable.length > 0 ? showable[0].name : null
        }

        return null
    }

    const selectTab = (tabName) => {
        setSelectedTab(tabName)
    }

    const classForButton = (buttonName) => {
        let c = "button ";
        if (selectedTab === buttonName) {
            c += "is-selected has-background-grey-lighter";
        } else if (selectedTab !== null && selectedTab !== undefined) {
            c += " has-selected"
        }

        return c;
    }

    const classForBody = (name) => {
        if (!useTabs || selectedTab === name) {
            return "";
        }

        return "is-hidden"
    }

    const buttonFor = (text, idx) => {
        return <button
            key={idx}
            className={classForButton(text)}
            onClick={() => selectTab(text)}>{text}
        </button>;
    }

    const withTabs = () => {
        return <div>
            <div className="buttons has-addons are-small">
                <p className="control">
                    {
                        values
                            .filter(shouldShow)
                            .map((value, idx) => buttonFor(value.name, idx))
                    }
                </p>
            </div>
            {
                values
                    .filter(shouldShow)
                    .map((value, idx) => {
                        return <RenderableValue key={idx}
                                                className={classForBody(value.name)}
                                                value={value}
                                                highlights={highlights}
                                                // interactionRef={interactionRef}
                        />;
                    })
            }
        </div>
    }

    const noTabs = () => {
        const value = values[0]
        // debugger
        return <RenderableValue key={1}
                                className={classForBody(value.name)}
                                value={value}
                                highlights={highlights}
                                // interactionRef={interactionRef}
        />;
    }

    useEffect(() => {
        if(useTabs) {
            setSelectedTab(firstNameOrNull())
        }
    }, []);

    return useTabs ? withTabs() : noTabs()
}