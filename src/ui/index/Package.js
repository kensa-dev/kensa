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
            return "idx-" + ((filter.type === "State" && filter.value !== "All") ? filter.value : pkg.state).toLowerCase().replaceAll(" ", "-")
        } else {
            return "is-hidden"
        }
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
                    <Package key={index} pkg={pkg} parentIsExpanded={isExpanded} filter={filter} />
                )
            }
            {
                pkg.classes && pkg.classes.map((testClass, index) =>
                    <Class key={index} testClass={testClass} parentIsExpanded={isExpanded}/>
                )
            }
        </dd>
    </dl>
}

export default Package