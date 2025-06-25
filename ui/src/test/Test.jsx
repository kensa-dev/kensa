import React, {useState} from "react";
import Invocation from "./invocation/Invocation";
import {CollapseIcon, stateClassFor} from "@/Util";
import Information from "./information/Information";
import Issues from "@/test/information/Issues";

const isDisabled = (test) => ['Disabled', 'Not Executed'].includes(test.state)

const TestContent = ({test}) =>
    (isDisabled(test)) ?
        <div className="message-body has-text-dark-grey">
            Test was not executed.
        </div>
        :
        <div className="message-body has-text-white">
            <Information notes={test.notes}/>
            {
                test.invocations.map((invocation, idx) =>
                    <Invocation key={idx} invocation={invocation} autoOpenTab={test.autoOpenTab}/>
                )
            }
        </div>

const Test = ({test, startExpanded}) => {
    const [isCollapsed, setCollapsed] = useState(!startExpanded);

    const toggle = () => setCollapsed(prev => !prev);

    return <div className={"message " + stateClassFor(test.state)}>
        <div onClick={toggle} className="message-header has-text-white">
            <div className={"limited-width"}>
                <span>{test.displayName}</span>
            </div>
            <div className="tags">
                <Issues issues={test.issues}/>
                {isDisabled(test) || <div className={"tag is-tiny"}>Elapsed: {test.elapsedTime}</div>}
                <CollapseIcon isCollapsed={isCollapsed}/>
            </div>
        </div>
        {isCollapsed || <TestContent test={test}/>}
    </div>
};

export default Test