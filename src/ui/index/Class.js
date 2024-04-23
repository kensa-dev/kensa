import React, {useState} from "react";
import {ExpandIcon} from "../Util";

const Class = ({testClass, parentIsExpanded}) => {
    const [isExpanded, setExpanded] = useState(false);

    const load = (anchor) => () => {
        window.location = "./" + testClass.fullClassName + ".html" + (anchor ? "#" + anchor : "");
    }

    const toggle = () => setExpanded(prev => !prev)

    if (parentIsExpanded && testClass.isVisible) {
        return (
            <dl className={testClass.cssCls}>
                <dt>
                    <ExpandIcon isExpanded={isExpanded} onClick={toggle}/>
                    <a onClick={load()}>{testClass.name}</a>
                </dt>
                {isExpanded && testClass.tests.filter(e => e.isVisible).map((entry, idx) =>
                    <dd className={entry.cssCls} key={idx}>
                        <a onClick={load(entry.method)}>{entry.name}</a>
                    </dd>
                )
                }
            </dl>
        )
    }
}


export default Class