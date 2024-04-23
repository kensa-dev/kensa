import React, {useState} from "react";
import {NamedValueTable} from "../NamedValueTable";

const RenderedAttributes = ({attributes, highlights}) => {
    const [selectedTab, selectTab] = useState(attributes[0]?.name);

    const btnClass = (name) => "button " + (selectedTab === name ? "is-selected has-background-grey-lighter" : " has-selected")
    const btnClick = (name) => () => selectTab(name)

    return <>
        <div className="buttons has-addons are-small">
            {
                attributes
                    .filter(ra => ra.attributes.length > 0)
                    .map((attribute, idx) =>
                        <button key={idx} className={btnClass(attribute.name)} onClick={btnClick(attribute.name)}>
                            {attribute.name}
                        </button>
                    )
            }
        </div>
        {
            attributes
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