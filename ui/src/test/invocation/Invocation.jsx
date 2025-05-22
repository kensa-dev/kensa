import React, {useContext, useState} from "react";
import {CollapseIcon, ConfigContext, Section, stateClassFor} from "@/Util";
import Tabs from "./tabs/Tabs";
import Sentences from "./sentence/Sentences";
import FailureMessage from "./FailureMessage";

const TestBody = ({invocation, testStateClass, autoOpenTab}) => {
    const {sectionOrder} = useContext(ConfigContext)
    return <>
        {
            sectionOrder.map((section, idx) => {
                    switch (section) {
                        case Section.Buttons:
                            return <Tabs key={idx} invocation={invocation} testStateClass={testStateClass} autoOpenTab={autoOpenTab}/>
                        case Section.Exception:
                            return <FailureMessage key={idx} invocation={invocation}/>
                        case Section.Sentences:
                            return <Sentences key={idx} invocation={invocation}/>
                    }
                }
            )
        }
    </>
}

const ParameterizedTestBody = ({invocation, testStateClass, ...props}) => {
    const [isCollapsed, setCollapsed] = useState(true)

    const toggle = () => setCollapsed(prev => !prev)

    return (
        <div className={"message " + testStateClass}>
            <div onClick={toggle} className="message-header has-text-white">
                <span className={"limited-width"}>{invocation.displayName}</span>
                <div className="tags">
                    <div className={"tag is-tiny"}>Elapsed: {invocation.elapsedTime}</div>
                    <CollapseIcon isCollapsed={isCollapsed}/>
                </div>
            </div>
            {isCollapsed || <div className={"message-body"}>{props.children}</div>}
        </div>
    )
}

const Invocation = ({invocation, autoOpenTab}) => {
    const testStateClass = stateClassFor(invocation.state);

    const testBody = <TestBody invocation={invocation} testStateClass={testStateClass} autoOpenTab={autoOpenTab}/>

    return (invocation['parameters']?.length)
        ? <ParameterizedTestBody invocation={invocation} testStateClass={testStateClass}>{testBody}</ParameterizedTestBody>
        : testBody
}

export default Invocation