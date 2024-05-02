import React, {useContext} from "react";
import {ConfigContext} from "../../Util";

const Issues = ({issues}) => {

    const IssueTag = ({issue}) => {
        const {issueTrackerUrl} = useContext(ConfigContext)
        const issueCls = "tag is-small has-background-grey has-text-white"

        return issueTrackerUrl ?
            <a className={issueCls} href={issueTrackerUrl.replace(/\/?$/, "/") + issue}>{issue}</a>
            :
            <span className={issueCls}>{issue}</span>;
    }

    if (issues.length > 0) {
        return <div className="tags">
            {issues.map((issue, idx) => <IssueTag key={idx} issue={issue}/>)}
        </div>
    }
}

export default Issues