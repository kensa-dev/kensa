import React, {useEffect, useState} from "react";
import Class from "./Class";
import {ExpandIcon} from "../Util";

const Package = ({pkg, parentIsExpanded, filter}) => {
    const [isExpanded, setExpanded] = useState(pkg.expanded);

    const toggle = () => setExpanded(prev => !prev)

    useEffect(() => {
        setExpanded(pkg.expanded)
    }, [pkg.expanded]);

    if (parentIsExpanded && pkg.isVisible) {
        return <dl className={pkg.cssCls}>
            <dt>
                <ExpandIcon isExpanded={isExpanded} onClick={toggle}/>
                {pkg.name}
            </dt>
            <dd>
                {
                    pkg.packages && pkg.packages.filter(p => p.isVisible).map((pkg, index) =>
                        <Package key={index} pkg={pkg} parentIsExpanded={isExpanded} filter={filter}/>
                    )
                }
                {
                    pkg.classes && pkg.classes.filter(c => c.isVisible).map((testClass, index) =>
                        <Class key={index} testClass={testClass} parentIsExpanded={isExpanded} filter={filter}/>
                    )
                }
            </dd>
        </dl>
    }
}

export default Package