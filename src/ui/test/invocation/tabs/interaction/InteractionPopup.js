import React, {useEffect, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import InteractionContent from "./InteractionContent";

export const InteractionPopup = ({onHide, capturedInteraction, highlights}) => {
    const fontSizes = ["font-normal", "font-large"]
    const [fontSizeIdx, setFontSizeIdx] = useState(0);

    useEffect(() => {
        document.addEventListener("keydown", onKeyDown, false);

        return () => {
            document.removeEventListener("keydown", onKeyDown, false);
        }
    })

    const togglePresentationSize = () => {
        setFontSizeIdx(fontSizeIdx === 0 ? 1 : 0)
    }

    const onKeyDown = (event) => {
        if (event.key === "Escape") {
            onHide();
        }
    }

    if (capturedInteraction) {
        return (
            <div className={"modal is-active"}>
                <div className="modal-background" onClick={onHide}/>
                <div className="modal-card">
                    <header className="modal-card-head">
                        <p className="modal-card-title">{capturedInteraction.name}</p>
                        <button className={"button is-info is-small mr-5"} onClick={togglePresentationSize}>Demo</button>
                        <a onClick={onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                    </header>
                    <section className={"modal-card-body " + fontSizes[fontSizeIdx]}>
                        <InteractionContent interaction={capturedInteraction["rendered"]} highlights={highlights}/>
                    </section>
                </div>
            </div>
        )
    }
}