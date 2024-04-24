import React, {useState} from "react";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const Class = ({testClass, parentIsExpanded, filter}) => {
    const [isExpanded, setExpanded] = useState(false);

    const icon = () => isExpanded ? faMinus : faPlus

    const load = (anchor) => {
        let url = "./" + testClass.container.testClass + ".html";
        if (anchor) {
            url += "#" + anchor;
        }
        window.location = url;
    }

    const toggle = () => {
        setExpanded(prev => !prev)
    }

    const deriveClassFor = (testClass) => {
        if (testClass.matched && parentIsExpanded) {
            if (testClass.state === "Passed" || filter.type === "State" && filter.value === "Passed") {
                return "idx-passed"
            }

            if (testClass.state === "Failed" || filter.type === "State" && filter.value === "Failed") {
                return "idx-failed"
            }

            if (testClass.state === "Disabled" || filter.type === "State" && filter.value === "Disabled") {
                return "idx-disabled"
            }
        }

        return "is-hidden"
    }

    const isHidden = () => isExpanded ? "" : "is-hidden"

    return (
        <dl className={deriveClassFor(testClass)}>
            <dt>
                <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
                <a onClick={() => load()}>{testClass.name}</a>
            </dt>
            {testClass.tests.length > 0 &&
                testClass.tests.map((entry, index) => {
                    return <dd key={index}>
                        <a onClick={() => load(entry.method)} className={isHidden()}>{entry.name}</a>
                    </dd>
                })
            }
        </dl>);
}

export default Class