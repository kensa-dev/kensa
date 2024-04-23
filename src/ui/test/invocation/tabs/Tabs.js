import React, {useState} from "react";
import {hasElements} from "../../../Util";
import {NamedValueTable} from "../../NamedValueTable";
import {SequenceDiagram} from "./SequenceDiagram";
import CapturedInteractions from "./CapturedInteractions";

const splitAndCapitalize = (s) => (s.charAt(0).toUpperCase() + s.slice(1)).split(/(?=[A-Z])/).join(" ")

const Button = ({name, deriveClass, doClick: setSelected}) =>
    <button key={name} className={deriveClass(name)} onClick={() => setSelected(prev => (prev === name) ? "" : name)}>{splitAndCapitalize(name)}</button>

const Tabs = ({invocation, testStateClass}) => {
    const [selected, setSelected] = useState()

    const deriveClass = (name) => {
        let c = "button ";
        if (selected === name) {
            c += "is-selected " + testStateClass;
        } else if (selected !== null && selected !== undefined) {
            c += " has-selected"
        }

        if (!hasElements(invocation, name)) {
            c += " is-hidden"
        }

        return c;
    }

    const TabContent = () => {
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
                return <CapturedInteractions invocationState={invocation.state}
                                             capturedInteractions={invocation.capturedInteractions}
                                             highlights={invocation.highlights}/>
            case 'sequenceDiagram':
                return <SequenceDiagram sequenceDiagram={invocation.sequenceDiagram}
                                        capturedInteractions={invocation.capturedInteractions}
                                        invocationState={invocation.state}
                                        highlights={invocation.highlights}/>
        }
    }

    return <>
        <div className="buttons has-addons">
            <Button name={'givens'} deriveClass={deriveClass} selected={selected} doClick={setSelected}/>
            <Button name={'parameters'} deriveClass={deriveClass} selected={selected} doClick={setSelected}/>
            <Button name={'capturedInteractions'} deriveClass={deriveClass} selected={selected} doClick={setSelected}/>
            <Button name={'sequenceDiagram'} deriveClass={deriveClass} selected={selected} doClick={setSelected}/>
        </div>
        <TabContent/>
    </>
}

export default Tabs