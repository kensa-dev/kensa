import React, {useState} from "react";
import Invocation from "./invocation/Invocation";
import {CollapseIcon, stateClassFor} from "../Util";
import Information from "./information/Information";

const isDisabled = (test) => ['Disabled', 'Not Executed'].includes(test.state)

const TestContent = ({test}) =>
    (isDisabled(test)) ?
        <div className={"message-body"}>
            Test was not executed.
        </div>
        :
        <div className={"message-body"}>
            <Information notes={test.notes} issues={test.issues}/>
            {
                test.invocations.map((invocation, idx) =>
                    <Invocation key={idx} invocation={invocation}/>
                )
            }
        </div>

const Test = ({test, startExpanded}) => {
    const [isCollapsed, setCollapsed] = useState(!startExpanded);

    const toggle = () => setCollapsed(prev => !prev);

    return <div className={"message " + stateClassFor(test.state)}>
        <div onClick={toggle} className="message-header">
            <span className={"limited-width"}>{test.displayName}</span>
            <div>
                {isDisabled(test) || <span className={"elapsed-time"}>Elapsed time: {test.elapsedTime}</span>}
                <CollapseIcon isCollapsed={isCollapsed}/>
            </div>
        </div>
        {isCollapsed || <TestContent test={test}/>}
    </div>
};

export default Test