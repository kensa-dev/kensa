import React, {useState} from "react";
import StackTracePopup from "./StackTracePopup";

const FailureMessage = ({invocation}) => {
    const [isActive, hideStackTrace] = useState(false)

    const executionException = invocation.executionException

    const hidePopup = () => hideStackTrace(false)
    const showPopup = () => hideStackTrace(true)

    if (executionException["message"]) {
        return (<>
            <div className="failure-message">{executionException.message}</div>
            <button className="button is-danger is-small" onClick={showPopup}>Stacktrace...</button>
            <StackTracePopup isActive={isActive} onHide={hidePopup} stackTrace={executionException.stackTrace}/>
        </>)
    }
}

export default FailureMessage