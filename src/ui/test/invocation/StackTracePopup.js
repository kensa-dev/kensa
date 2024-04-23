import React, {useEffect, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";

const StackTracePopup = ({stackTrace, onHide, ...props}) => {
    const onKeyDown = (event) => {
        if (event.keyCode === 27) {
            onHide();
        }
    }

    useEffect(() => {
        document.addEventListener("keydown", onKeyDown, false);

        return () => {
            document.removeEventListener("keydown", onKeyDown, false);
        }
    })

    return (
        <div className={"modal " +( props.isActive ? "is-active" : "")}>
            <div className="modal-background" onClick={onHide}/>
            <div className="modal-card">
                <header className="modal-card-head">
                    <p className="modal-card-title">Stacktrace</p>
                    <a onClick={onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                </header>
                <section className={"modal-card-body"}>
                    <div className="stack-trace">{stackTrace}</div>
                </section>
            </div>
        </div>
    )
}

export default StackTracePopup