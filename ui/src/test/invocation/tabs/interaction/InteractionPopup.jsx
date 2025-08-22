import React, {useEffect, useRef} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import InteractionContent from "./InteractionContent";

export const InteractionPopup = ({onHide, capturedInteraction, highlights}) => {
    const modalRef = useRef(null);

    useEffect(() => {
        // Focus the modal when it becomes active
        if (modalRef.current) {
            modalRef.current.focus();
        }
    }, []);

    const handleEscape = (event) => {
        if (event.key === "Escape") {
            onHide();
        }
    }

    if (capturedInteraction) {
        return (
            <div className={"modal is-active"} tabIndex="0" onKeyDown={handleEscape} ref={modalRef}>
                <div className="modal-background" onClick={onHide}/>
                <div className="modal-card">
                    <header className="modal-card-head">
                        <p className="modal-card-title">{capturedInteraction.name}</p>
                        <span className="icon is-small has-text-grey">
                            <a onClick={onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                        </span>
                    </header>
                    <section className="modal-card-body">
                        <InteractionContent interaction={capturedInteraction["rendered"]} highlights={highlights}/>
                    </section>
                </div>
            </div>
        )
    }
}