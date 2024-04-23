import React, {useState, useEffect, createRef} from "react";
import {faTimesCircle} from "@fortawesome/free-solid-svg-icons/faTimesCircle";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {RenderableValues} from "../../RenderableValues";
import {RenderableAttributes} from "../../RenderableAttributes";

export const Popup = ({onHide, interaction, highlights}) => {
    const fontSizes = ["font-normal", "font-large"]
    const [fontSizeIdx, setFontSizeIdx] = useState(0);

    const togglePresentationSize = () => {
        setFontSizeIdx(fontSizeIdx === 0 ? 1 : 0)
    }

    const onKeyDown = (event) => {
        if (event.keyCode === 27) {
            onHide();
        }
    }

    useEffect(() => {
        // Fired on component mount
        document.addEventListener("keydown", onKeyDown, false);

        return () => {
            // Fired on component unmount
            document.removeEventListener("keydown", onKeyDown, false);
        }
    })

    if (interaction) {
        let renderedAttributes = interaction["rendered"]["attributes"]
        let renderedValues = interaction["rendered"]["values"]

        return (
            <div className={"modal is-active"}>
                <div className="modal-background" onClick={onHide}/>
                <div className="modal-card">
                    <header className="modal-card-head">
                        <p className="modal-card-title">{interaction.name}</p>
                        <button className={"button is-info is-small mr-5"} onClick={togglePresentationSize}>Demo
                        </button>
                        <a onClick={onHide}><FontAwesomeIcon icon={faTimesCircle}/></a>
                    </header>
                    <section className={"modal-card-body " + fontSizes[fontSizeIdx]}>
                        <RenderableAttributes highlights={highlights}
                                              attributes={renderedAttributes}
                                              isSequenceDiagram={true}/>
                        <RenderableValues highlights={highlights}
                                          values={renderedValues}
                                          isSequenceDiagram={true}/>
                    </section>
                </div>
            </div>
        )
    }
    return null;
}

export const SequenceDiagram = ({capturedInteractions, invocationState, highlights, sequenceDiagram}) => {
    const sequenceDiagramRef = createRef();
    const [isPopupActive, setPopupActive] = useState(false);
    const [interaction, setInteraction] = useState(null);

    const findClickableChildrenOf = (parent, clickableNodes) => {
        Array.from(parent.childNodes).forEach(child => {
                if (child.classList && child.classList.contains('sequence_diagram_clickable')) {
                    clickableNodes.push(child);
                }
                findClickableChildrenOf(child, clickableNodes);
            }
        );
    }

    const findInteractionFor = (target) => {
        let interactionId = target.getAttribute("sequence_diagram_interaction_id");

        return capturedInteractions.find(interaction => {
            return interaction.id === interactionId;
        });
    }

    const onClick = (e) => {
        let interaction = findInteractionFor(e.target);

        setPopupActive(true)
        setInteraction(interaction)
    }

    const hidePopup = () => {
        setPopupActive(false)
        setInteraction(null)
    }

    const popup = () => {
        if (isPopupActive) {
            return <Popup onHide={hidePopup}
                          invocationState={invocationState}
                          interaction={interaction}
                          highlights={highlights}/>
        }
    }

    useEffect(() => {
        const svgNode = sequenceDiagramRef.current.firstElementChild;
        const clickableNodes = []
        findClickableChildrenOf(svgNode, clickableNodes);

        clickableNodes.forEach(node => {
            node.addEventListener('click', onClick);
        })

        return () => {
            clickableNodes.forEach(node => {
                node.removeEventListener('click', onClick);
            })
        }
    }, []);

    return (
        <div>
            <div ref={sequenceDiagramRef} className="sequence-diagram"
                 dangerouslySetInnerHTML={{__html: sequenceDiagram}}/>
            {popup()}
        </div>
    )
}