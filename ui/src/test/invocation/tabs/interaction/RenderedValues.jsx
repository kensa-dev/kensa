import React, {createRef, useEffect, useState} from "react";
import Lowlight from "react-lowlight";
import {highlight} from "../../Highlighting";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCopy} from "@fortawesome/free-solid-svg-icons";
import "./RenderedValues.scss"

const RenderedValue = ({highlights, value}) => {
    const interactionRef = createRef();
    const language = value.language ? value.language : "plainText";

    useEffect(() => {
        const parent = interactionRef.current.children[0].firstChild
        highlight(language, parent, highlights)
    }, []);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(value.value);
        } catch (err) {
            console.error('Failed to copy text: ', err);
        }
    };

    return (
        <div
            className="rendered-value"
            ref={interactionRef}
            style={{position: 'relative'}}
        >
            <button className="button bd-copy" onClick={handleCopy}>
                <span className="icon">
                    <FontAwesomeIcon icon={faCopy}/>
                </span>
            </button>
            <Lowlight className="scrollable-value" language={language} value={value.value}/>
        </div>
    );
};

const RenderedValues = ({values, highlights}) => {

    const WithTabs = () => {
        const [selectedTab, selectTab] = useState(values[0]);

        const btnClass = (name) => "button " + (selectedTab === name ? "is-selected" : "has-selected")
        const btnClick = (name) => () => selectTab(prev => (prev === name) ? "" : name)

        return <>
            <div className="buttons has-addons are-small">
                {
                    values.map((value, idx) =>
                        <button key={idx} className={btnClass(value.name)} onClick={btnClick(value.name)}>{value.name}</button>
                    )
                }
            </div>
            {
                values.map((value, idx) =>
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