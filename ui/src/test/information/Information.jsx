import Notes from "./Notes";
import React from "react";

const Information = ({notes}) => {
    if (notes) {
        return (
            <div className="message notes">
                <div className="message-body">
                    <Notes notes={notes}/>
                </div>
            </div>
        )
    }
}

export default Information