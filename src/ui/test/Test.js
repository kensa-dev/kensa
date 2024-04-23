import React, {useEffect, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Invocation from "./invocation/Invocation";
import {collapseIcon, stateClassFor} from "../Util";
import {Information} from "./Information";

const Test = ({issueTrackerUrl, sectionOrder, test, startExpanded}) => {
    console.log(startExpanded)
    const [isCollapsed, setCollapsed] = useState(!startExpanded);

    const toggle = () => setCollapsed(prev => !prev);

    if (test.state === 'Disabled' || test.state === 'Not Executed') {
        return (
            <div className={"message " + stateClassFor(test.state)}>
                <div onClick={toggle} className="message-header">
                    <span className={"limited-width"}>{test.displayName}</span>
                    <a><FontAwesomeIcon icon={collapseIcon(isCollapsed)}/></a>
                </div>
                {
                    !isCollapsed ?
                        <div className={"message-body"}>
                            Test was not executed.
                        </div>
                        : null
                }
            </div>
        );
    } else {
        return (
            <div className={"message " + stateClassFor(test.state)}>
                <div onClick={toggle} className="message-header">
                    <span className={"limited-width"}>{test.displayName}</span>
                    <div>
                        <span className={"elapsed-time"}>Elapsed time: {test.elapsedTime}</span>
                        <a><FontAwesomeIcon icon={collapseIcon(isCollapsed)}/></a>
                    </div>
                </div>
                {!isCollapsed ?
                    <div className={"message-body"}>
                        <Information issueTrackerUrl={issueTrackerUrl} notes={test.notes} issues={test.issues}/>
                        {
                            test.invocations.map((invocation, index) =>
                                <Invocation key={index}
                                            sectionOrder={sectionOrder}
                                            invocation={invocation}/>
                            )
                        }
                    </div>
                    : null
                }
            </div>
        );
    }
};

export default Test