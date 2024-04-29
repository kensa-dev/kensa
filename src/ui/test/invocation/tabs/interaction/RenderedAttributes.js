import React, {useEffect, useState} from "react";
import {NamedValueTable} from "../../../NamedValueTable";

const RenderedAttributes = ({isSequenceDiagram, attributes, highlights}) => {
    const [selectedTab, selectTab] = useState(null);

    useEffect(() => {
        let firstTab = attributes.find(shouldShow);
        selectTab(firstTab?.name)
    }, []);

    const shouldShow = (item) => (isSequenceDiagram && item.showOnSequenceDiagram) || !isSequenceDiagram
    const btnClass = (name) => "button " + (selectedTab === name ? "is-selected has-background-grey-lighter" : " has-selected")
    const btnClick = (name) => () => selectTab(name)

    return <>
        <div className="buttons has-addons are-small">
            {
                attributes
                    .filter(shouldShow)
                    .filter(ra => ra.attributes.length > 0)
                    .map((attribute, idx) =>
                        <button key={idx} className={btnClass(attribute.name)} onClick={btnClick(attribute.name)}>{attribute.name}</button>
                    )
            }
        </div>
        {
            attributes
                .filter(shouldShow)
                .filter(a => a.name === selectedTab)
                .map((attribute, idx) =>
                    <div key={idx} className={"rendered-attribute"}>
                        <NamedValueTable showHeader={false} highlights={highlights} namedValues={attribute.attributes}/>
                    </div>
                )
        }
    </>
}

export default RenderedAttributes;