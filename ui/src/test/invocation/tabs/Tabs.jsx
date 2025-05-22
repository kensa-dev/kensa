import React, {useState} from "react";
import {NamedValueTable} from "./NamedValueTable";
import {SequenceDiagram} from "./SequenceDiagram";
import CapturedInteractions from "./CapturedInteractions";
import {Tab} from "@/Util";

const Button = ({name, deriveClass, onClick}) => {
    const splitAndCapitalize = (s) => (s.charAt(0).toUpperCase() + s.slice(1)).split(/(?=[A-Z])/).join(" ")

    return <button key={name} className={"button " + deriveClass(name)} onClick={onClick}>{splitAndCapitalize(name)}</button>;
}

const TabContent = ({selected, invocation}) => {
    switch (selected) {
        case 'givens':
            return <NamedValueTable showHeader={true}
                                    highlights={invocation.highlights}
                                    namedValues={invocation.givens}/>
        case 'parameters' :
            return <NamedValueTable showHeader={true}
                                    highlights={invocation.highlights}
                                    namedValues={invocation.parameters}/>
        case 'capturedInteractions':
            return <CapturedInteractions capturedInteractions={invocation.capturedInteractions}
                                         highlights={invocation.highlights}/>
        case 'sequenceDiagram':
            return <SequenceDiagram sequenceDiagram={invocation.sequenceDiagram}
                                    capturedInteractions={invocation.capturedInteractions}
                                    highlights={invocation.highlights}/>
    }
}

const Tabs = ({invocation, testStateClass, autoOpenTab}) => {
    const [selected, setSelected] = useState(Tab[autoOpenTab])
    const btnClass = (name) => selected === name ? "is-selected " + testStateClass : "has-selected"
    const btnClick = (name) => () => setSelected(prev => (prev === name) ? "" : name)

    return <>
        <div className="buttons has-addons">
            {
                Object.values(Tab)
                    .filter(b => invocation[b]?.length)
                    .map((name, idx) =>
                        <Button key={idx} name={name} deriveClass={btnClass} onClick={btnClick(name)}/>
                    )
            }
        </div>
        <TabContent selected={selected} invocation={invocation}/>
    </>
}

export default Tabs