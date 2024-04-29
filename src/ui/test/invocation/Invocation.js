import React, {useContext, useState} from "react";
import {CollapseIcon, Section, SectionOrderContext, stateClassFor} from "../../Util";
import Tabs from "./tabs/Tabs";
import Sentences from "./sentence/Sentences";
import FailureMessage from "./FailureMessage";

const TestBody = ({invocation, testStateClass}) => {
    const sectionOrder = useContext(SectionOrderContext)
    return <>
        {
            sectionOrder.map((section, index) => {
                    switch (section) {
                        case Section.Buttons:
                            return <Tabs key={index} invocation={invocation} testStateClass={testStateClass}/>
                        case Section.Exception:
                            return <FailureMessage key={index} invocation={invocation}/>
                        case Section.Sentences:
                            return <Sentences key={index} invocation={invocation}/>
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
            <div onClick={toggle} className={"message-header"}>
                <span className={"limited-width"}>{invocation.parameterizedTestDescription}</span>
                <div>
                    <span className={"elapsed-time"}>Elapsed time: {invocation.elapsedTime}</span>
                    <CollapseIcon isCollapsed={isCollapsed}/>
                </div>
            </div>
            {isCollapsed || <div className={"message-body"}>{props.children}</div>}
        </div>
    )
}

const Invocation = ({invocation}) => {
    const testStateClass = stateClassFor(invocation.state);

    const testBody = <TestBody invocation={invocation} testStateClass={testStateClass}/>

    return (invocation['parameters']?.length)
        ? <ParameterizedTestBody invocation={invocation} testStateClass={testStateClass}>{testBody}</ParameterizedTestBody>
        : testBody
}

export default Invocation