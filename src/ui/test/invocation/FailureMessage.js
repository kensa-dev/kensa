import React, {useState} from "react";
import StackTracePopup from "./StackTracePopup";

const FailureMessage = ({invocation}) => {
    const [isActive, hideStackTrace] = useState(false)

    const executionException = invocation.executionException
    const stackTrace = executionException.stackTrace

    if (executionException["message"]) {
        return (<>
            <div className="failure-message">{executionException.message}</div>
            <button className="button is-danger is-small" onClick={() => hideStackTrace(true)}>Stacktrace...</button>
            <StackTracePopup isActive={isActive} onHide={() => hideStackTrace(false)} stackTrace={stackTrace}/>
        </>)
    }

    return null;
}

export default FailureMessage