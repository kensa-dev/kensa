import React, {useState} from "react";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const Class = ({testClass, parentIsExpanded}) => {
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

    const deriveClassFor = (testClass) => {
        if (testClass.matched && parentIsExpanded) {
            return "idx-" + testClass.state.toLowerCase().replaceAll(" ", "-")
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