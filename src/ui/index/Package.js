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

    const deriveClassFor = (pkg) =>
        "idx-" + ((filter.state !== "All") ? filter.state : pkg.state).toLowerCase().replaceAll(" ", "-")

    useEffect(() => {
        setExpanded(pkg.expanded)
    }, [pkg.expanded]);

    if (pkg.matched && parentIsExpanded) {
        return <dl className={deriveClassFor(pkg)}>
            <dt>
                <span className="idx-icon" onClick={toggle}><FontAwesomeIcon icon={icon()}/></span>
                {pkg.name}
            </dt>
            <dd>
                {
                    pkg.packages && pkg.packages.filter(p => p.matched).map((pkg, index) =>
                        <Package key={index} pkg={pkg} parentIsExpanded={isExpanded} filter={filter}/>
                    )
                }
                {
                    pkg.classes && pkg.classes.filter(c => c.matched).map((testClass, index) =>
                        <Class key={index} testClass={testClass} parentIsExpanded={isExpanded} filter={filter}/>
                    )
                }
            </dd>
        </dl>
    }
}

export default Package