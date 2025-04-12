import Notes from "./Notes";
import React from "react";
import Issues from "./Issues";

const Information = ({issues, notes}) => {
    if (issues.length > 0 || notes) {
        return (
            <div className="message is-info">
                <div className="message-body">
                    <Issues issues={issues}/>
                    <Notes notes={notes}/>
                </div>
            </div>
        )
    }
}

export default Information