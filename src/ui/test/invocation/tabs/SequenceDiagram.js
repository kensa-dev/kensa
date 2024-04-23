import React, {createRef, useEffect, useState} from "react";
import {InteractionPopup} from "./interaction/InteractionPopup";

export const SequenceDiagram = ({capturedInteractions, highlights, sequenceDiagram}) => {
    const sequenceDiagramRef = createRef();
    const [interaction, setInteraction] = useState(null);

    const clickableChildrenOf = (parent) => {
        const clickableNodes = []
        parent.childNodes.forEach(child => {
                if (child.classList && child.classList.contains('sequence_diagram_clickable')) {
                    clickableNodes.push(child);
                }
                clickableNodes.push(...clickableChildrenOf(child));
            }
        );
        return clickableNodes
    }

    const onClick = (e) => {
        let interactionId = e.target.getAttribute("sequence_diagram_interaction_id");

        let interaction = capturedInteractions.find(i => i.id === interactionId);
        setInteraction(interaction)
    }

    const hidePopup = () => setInteraction(null)

    useEffect(() => {
        const clickableNodes = clickableChildrenOf(sequenceDiagramRef.current.firstElementChild);

        clickableNodes.forEach(node => {
            node.addEventListener('click', onClick);
        })

        return () => {
            clickableNodes.forEach(node => {
                node.removeEventListener('click', onClick);
            })
        }
    }, []);

    return <>
        <div ref={sequenceDiagramRef}
             className="sequence-diagram"
             dangerouslySetInnerHTML={{__html: sequenceDiagram}}/>
        {
            interaction &&
            <InteractionPopup onHide={hidePopup}
                              capturedInteraction={interaction}
                              highlights={highlights}
            />
        }
    </>
}