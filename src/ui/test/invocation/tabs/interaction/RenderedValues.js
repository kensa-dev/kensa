import React, {createRef, useEffect, useState} from "react";
import Lowlight from "react-lowlight";
import {joinForRegex} from "../../../../Util";
import {highlightJson, highlightPlainText, highlightXml} from "../../../Highlighting";

const RenderedValue = ({highlights, value}) => {
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

    return <div className={"rendered-value"}>
        <div ref={interactionRef}>
            <Lowlight className={"scrollable-value"} language={language} value={value.value}/>
        </div>
    </div>
}

const RenderedValues = ({isSequenceDiagram, values, highlights}) => {
    const [selectedTab, selectTab] = useState(null);

    useEffect(() => {
        if (values.length > 1) {
            selectTab(values.filter(shouldShow))
        }
    }, []);

    const shouldShow = (item) => (isSequenceDiagram && item.showOnSequenceDiagram) || !isSequenceDiagram
    const btnClass = (name) => "button " + (selectedTab === name ? "is-selected" : "has-selected")
    const btnClick = (name) => () => selectTab(prev => (prev === name) ? "" : name)

    const WithTabs = () => {
        return <>
            <div className="buttons has-addons are-small">
                {
                    values
                        .filter(shouldShow)
                        .map((value, idx) =>
                            <button key={idx} className={btnClass(value.name)} onClick={btnClick(value.name)}>name</button>
                        )
                }
            </div>
            {
                values
                    .filter(shouldShow)
                    .map((value, idx) =>
                        <RenderedValue key={idx}
                                       value={value}
                                       highlights={highlights}
                        />
                    )
            }
        </>
    }

    const NoTabs = () =>
        <RenderedValue key={0}
                       value={values[0]}
                       highlights={highlights}
        />

    return values.length > 1 ? <WithTabs/> : <NoTabs/>
}

export default RenderedValues
