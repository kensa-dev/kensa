import "./sequence-diagram.scss";
import React, {createRef, useEffect, useState} from "react";
import {InteractionPopup} from "./interaction/InteractionPopup";

export const SequenceDiagram = ({capturedInteractions, highlights, sequenceDiagram}) => {
    const sequenceDiagramRef = createRef();
    const sequenceDiagramHeaderRef = createRef();
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

    const addFloatingHeader = () => {
        const originalSvg = sequenceDiagramRef.current.querySelector('svg');
        const newSvg = originalSvg.cloneNode(true);

        // Remove all SVG elements except the top row of <rects> and <text>s
        newSvg.querySelectorAll('line, .sequence_diagram_clickable, path, polygon').forEach(element => element.remove());
        newSvg.querySelectorAll('rect:first-child, rect:nth-last-child(2), text:last-child').forEach(element => element.remove());
        newSvg.querySelectorAll('text, rect').forEach((element, idx) => { if (idx % 4 >= 2) element.remove() } );

        const firstRect = newSvg.querySelector('rect')
        const headerHeight = (parseInt(firstRect.getAttribute('y'))*2) + parseInt(firstRect.getAttribute('height'));

        newSvg.style.height = `${headerHeight}px`;
        newSvg.setAttribute('viewBox', newSvg.getAttribute('viewBox').split(' ').with(3, `${headerHeight}`).join(' '));
        newSvg.setAttribute('height', `${headerHeight}px`);

        sequenceDiagramHeaderRef.current.appendChild(newSvg);
        sequenceDiagramHeaderRef.current.setAttribute('height', `${headerHeight}`);

        sequenceDiagramRef.current.ownerDocument.addEventListener('scroll', updateFloatingHeaderPosition);
    }

    /// Using `position: sticky` doesn't work reliably in any browser when any ancestor has `overflow-x: auto` :(
    const updateFloatingHeaderPosition = () => {
        const windowHeight = window.innerHeight;
        const windowScrollTop = window.scrollY;

        const diagramContainer = sequenceDiagramRef.current;
        const diagramContainerTop = diagramContainer.offsetTop
        const diagramContainerHeight = diagramContainer.offsetHeight

        const header = sequenceDiagramHeaderRef.current;
        const headerHeight = parseInt(header.getAttribute('height'));

        const diagramIsTooBigToFitOnScreen = windowHeight < diagramContainerHeight;
        const userHasReachedDiagram = windowScrollTop >= diagramContainerTop;
        const userHasNotScrolledPastDiagram = windowScrollTop < diagramContainerTop + diagramContainerHeight - (headerHeight*2);

        if (diagramIsTooBigToFitOnScreen && userHasReachedDiagram && userHasNotScrolledPastDiagram) {
            header.style.top = `${windowScrollTop - diagramContainerTop}px`;
            header.style.display = 'block';
        }
        else
        {
            header.style.display = 'none';
        }
    }

    useEffect(() => {
        const sequenceDiagram = sequenceDiagramRef.current;
        const clickableNodes = clickableChildrenOf(sequenceDiagram);

        clickableNodes.forEach(node => {
            node.addEventListener('click', onClick);
        })

        addFloatingHeader();

        return () => {
            clickableNodes.forEach(node => {
                node.removeEventListener('click', onClick);
            })

            sequenceDiagram.ownerDocument.removeEventListener('scroll', updateFloatingHeaderPosition);
        }
    }, []);

    return <>
        <div ref={sequenceDiagramRef} className="sequence-diagram">
            <div ref={sequenceDiagramHeaderRef} className="sequence-diagram-header"></div>
            <div dangerouslySetInnerHTML={{__html: sequenceDiagram}}/>
        </div>
        {
            interaction &&
            <InteractionPopup onHide={hidePopup}
                              capturedInteraction={interaction}
                              highlights={highlights}
            />
        }
    </>
}