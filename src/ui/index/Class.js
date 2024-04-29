import React, {useState} from "react";
import {ExpandIcon} from "../Util";

const Class = ({testClass, parentIsExpanded, filter}) => {
    const [isExpanded, setExpanded] = useState(false);

    const load = (anchor) => () => {
        window.location = "./" + testClass.fullClassName + ".html" + (anchor ? "#" + anchor : "");
    }

    const toggle = () => setExpanded(prev => !prev)

    const classForTestClass = (cls) =>
        "idx-" + (filter.state || cls.state).toLowerCase().replaceAll(" ", "-")

    const classForTestMethod = (test) => "idx-" + test.state.toLowerCase()

    if (parentIsExpanded && testClass.matched) {
        return (
            <dl className={classForTestClass(testClass)}>
                <dt>
                    <ExpandIcon isExpanded={isExpanded} onClick={toggle}/>
                    <a onClick={load()}>{testClass.name}</a>
                </dt>
                {isExpanded && testClass.tests.filter(e => e.matched).map((entry, index) =>
                    <dd className={classForTestMethod(entry)} key={index}>
                        <a onClick={load(entry.method)}>{entry.name}</a>
                    </dd>)
                }
            </dl>
        )
    }
}


export default Class