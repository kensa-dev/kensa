import React, {useState} from "react";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const Class = ({testClass, parentIsExpanded, filter}) => {
    const [isExpanded, setExpanded] = useState(false);

    const icon = () => isExpanded ? faMinus : faPlus

    const load = (anchor) => {
        let url = "./" + testClass.fullClassName + ".html";
        if (anchor) {
            url += "#" + anchor;
        }
        window.location = url;
    }

    const toggle = () => {
        setExpanded(prev => !prev)
    }

    const classForTestClass = (cls) =>
        "idx-" + (filter.state || cls.state).toLowerCase().replaceAll(" ", "-")

    const classForTestMethod = (test) => "idx-" + test.state.toLowerCase()

    if (parentIsExpanded && testClass.matched) {
        return (
            <dl className={classForTestClass(testClass)}>
                <dt>
                    <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
                    <a onClick={() => load()}>{testClass.name}</a>
                </dt>
                {isExpanded && testClass.tests.filter(e => e.matched).map((entry, index) =>
                    <dd className={classForTestMethod(entry)} key={index}>
                        <a onClick={() => load(entry.method)}>{entry.name}</a>
                    </dd>)
                }
            </dl>
        )
    }
}


export default Class