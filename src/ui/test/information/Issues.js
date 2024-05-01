import React, {useContext} from "react";
import {TrackingUrlContext} from "../../Util";

const Issues = ({issues}) => {
    const issueTrackerUrl = useContext(TrackingUrlContext)

    const issueTrackerUrlFor = (issue) => issueTrackerUrl.replace(/\/?$/, "/") + issue

    if (issues.length > 0) {
        return <div className="tags">
            {
                issues.map((issue, idx) =>
                    <a key={idx} className={"tag is-small has-background-grey has-text-white"} href={issueTrackerUrlFor(issue)}>{issue}</a>)
            }
        </div>
    }
}

export default Issues