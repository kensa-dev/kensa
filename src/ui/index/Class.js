import React, {useContext, useState} from "react";
import {ConfigContext, ExpandIcon} from "../Util";

const Class = ({testClass, parentIsExpanded}) => {
    const [isExpanded, setExpanded] = useState(false);
    const {flattenPackages} = useContext(ConfigContext)
    const generateUrl = (testClass, anchor) => {
        const basePath = flattenPackages
            ? `${testClass.fullClassName}.html`
            : `./${testClass.fullClassName.split(".").join("/")}.html`;

        return anchor ? `${basePath}#${anchor}` : basePath;
    };

    const toggle = () => setExpanded(prev => !prev)

    if (parentIsExpanded && testClass.isVisible) {
        return (
            <dl className={testClass.cssCls}>
                <dt>
                    <ExpandIcon isExpanded={isExpanded} onClick={toggle}/>
                    <a href={generateUrl(testClass)}>{testClass.name}</a>
                </dt>
                {isExpanded && testClass.tests.filter(e => e.isVisible).map((entry, idx) =>
                    <dd className={entry.cssCls} key={idx}>
                        <a href={generateUrl(testClass, entry.method)}>{entry.name}</a>
                    </dd>
                )
                }
            </dl>
        )
    }
}


export default Class