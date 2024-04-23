import React, {useEffect, useState} from "react";
import {NamedValueTable} from "./NamedValueTable";

export const RenderableAttribute = ({attribute, highlights, className}) => {
    return (
        <div className={"renderable-attribute " + className}>
            <NamedValueTable showHeader={false} highlights={highlights} namedValues={attribute.attributes}/>
        </div>
    )
}

export const RenderableAttributes = ({isSequenceDiagram, attributes, highlights}) => {
    const [selectedTab, selectTab] = useState(null);
    const shouldShow = (attribute) => (isSequenceDiagram && attribute.showOnSequenceDiagram) || !isSequenceDiagram

    const firstNameOrNull = () => {
        if (attributes.length > 0) {
            let showable = attributes.filter(shouldShow);
            return showable.length > 0 ? showable[0].name : null
        }

        return null
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
        if (selectedTab === name) {
            return "";
        }

        return "is-hidden"
    }

    const buttonForAttribute = (text, idx) => {
        return <button
            key={idx}
            className={classForButton(text)}
            onClick={() => selectTab(text)}>{text}
        </button>;
    }

    useEffect(() => {
        selectTab(firstNameOrNull())

    }, []);

    return <>
        <div className="buttons has-addons are-small">
            <p className="control">
                {
                    attributes
                        .filter(shouldShow)
                        .map((attribute, idx) => {
                            return buttonForAttribute(attribute.name, idx)
                        })
                }
            </p>
        </div>
        {
            attributes
                .filter(shouldShow)
                .map((attribute, idx) =>
                    <RenderableAttribute key={idx}
                                         className={classForBody(attribute.name)}
                                         highlights={highlights}
                                         attribute={attribute}/>)
        }
    </>
}