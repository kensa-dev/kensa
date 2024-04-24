import React, {useEffect, useState} from "react";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import Class from "./Class";

const Package = ({pkg, parentIsExpanded, filter}) => {
    const [isExpanded, setExpanded] = useState(pkg.expanded);

    const icon = () => isExpanded ? faMinus : faPlus

    const toggle = () => {
        setExpanded(prev => !prev)
    }

    const deriveClassFor = (pkg) => {
        if (pkg.matched && parentIsExpanded) {
            if (pkg.state === "Passed" || filter.type === "State" && filter.value === "Passed") {
                return "idx-passed"
            }

            if (pkg.state === "Failed" || filter.type === "State" && filter.value === "Failed") {
                return "idx-failed"
            }

            if (pkg.state === "Disabled" || filter.type === "State" && filter.value === "Disabled") {
                return "idx-disabled"
            }
        }

        return "is-hidden"
    }

    useEffect(() => {
        setExpanded(pkg.expanded)
    }, [pkg.expanded]);

    return <dl className={deriveClassFor(pkg)}>
        <dt>
            <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
            {pkg.name}
        </dt>
        <dd>
            {
                pkg.packages && pkg.packages.map((pkg, index) =>
                    <Package key={index} pkg={pkg}
                             filter={filter}
                             parentIsExpanded={isExpanded}/>
                )
            }
            {
                pkg.classes && pkg.classes.map((testClass, index) =>
                    <Class key={index}
                           testClass={testClass}
                           filter={filter}
                           parentIsExpanded={isExpanded}/>
                )
            }
        </dd>
    </dl>
}

export default Package