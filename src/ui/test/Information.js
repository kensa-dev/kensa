import Notes from "./Notes";
import React from "react";

export const Information = ({issueTrackerUrl, issues, notes}) => {
    const issueTrackerUrlFor = (issue) => issueTrackerUrl.endsWith("/") ? issueTrackerUrl + issue : issueTrackerUrl + "/" + issue

    const Issues = () => {
        if (issues.length > 0) {
            return <div className="tags">
                {
                    issues.map((issue, idx) =>
                        <a key={idx} href={issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>)
                }
            </div>
        }
    }

    if (issues.length > 0 || notes) {
        return (
            <div className="message is-info">
                <div className="message-body">
                    <Issues/>
                    <Notes notes={notes}/>
                </div>
            </div>
        )
    }
}