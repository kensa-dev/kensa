import "./sequence-diagram.scss";
import React, {createRef, useEffect, useState} from "react";
import {InteractionPopup} from "./interaction/InteractionPopup";
import {flash, forAllCombinations} from "../../../Util";

export const SequenceDiagram = ({capturedInteractions, highlights, sequenceDiagram}) => {
    const sequenceDiagramRef = createRef();
    const [interaction, setInteraction] = useState(null);
    const [selectedActorNames, setSelectedActorNames] = useState([]);
    const [isNoMatchesForFilter, setIsNoMatchesForFilter] = useState(false);

    const clickableMessageChildrenOf = (parent) => {
        const clickableNodes = []
        parent.childNodes.forEach(child => {
                if (child.classList && child.classList.contains('sequence_diagram_clickable')) {
                    clickableNodes.push(child);
                }
                clickableNodes.push(...clickableMessageChildrenOf(child));
            }
        );
        return clickableNodes
    }

    const onMessageClick = (e) => {
        let interactionId = e.target.getAttribute("sequence_diagram_interaction_id");

        let interaction = capturedInteractions.find(i => i.id === interactionId);
        setInteraction(interaction)
    }

    const hidePopup = () => setInteraction(null)

    const onActorClick = (e) => {
        const clickedActorName = e.target.getAttribute('data-actor');

        const toggleItemInArray = (item, arr) => {
            if (arr.includes(item)) {
                return arr.filter(name => name !== item);
            } else {
                return arr.concat(item);
            }
        }

        setSelectedActorNames(prevState => toggleItemInArray(clickedActorName, prevState));
    }

    const updateHighlightedActorsAndMessages = () => {
        try {
            const sequenceDiagram = sequenceDiagramRef.current;
            sequenceDiagram.querySelectorAll('.actor, .msg').forEach((elem) => elem.classList.remove('filter-highlight'));

            const addHighlight = (elem) => elem.classList.add('filter-highlight');

            if (selectedActorNames.length === 0) {
                return;
            } else if (selectedActorNames.length === 1) {
                const actorName = selectedActorNames[0];
                sequenceDiagram.querySelectorAll(`.msg-from-${actorName}, .msg-to-${actorName}`).forEach(addHighlight);
            } else {
                forAllCombinations(selectedActorNames, (a, b) => {
                    sequenceDiagram.querySelectorAll(`.msg-from-${a}.msg-to-${b}, .msg-from-${b}.msg-to-${a}`).forEach(addHighlight);
                });
            }

            const addHighlightToAllActorElementsWithName = (actorName) => sequenceDiagram.querySelectorAll(`.actor-${actorName}`).forEach(addHighlight);
            selectedActorNames.forEach(addHighlightToAllActorElementsWithName);

            const firstHighlightedMessage = sequenceDiagram.querySelector('text.msg.filter-highlight');
            setIsNoMatchesForFilter(firstHighlightedMessage === null);

            if (firstHighlightedMessage !== null) {
                firstHighlightedMessage.scrollIntoView({'behavior': 'smooth', 'block': 'nearest'});
                flash(firstHighlightedMessage, 3);
            }
        } catch (e) {
            console.log('Failed to update highlights for selected actors', selectedActorNames, e);
        }
    }

    const actorChildrenOf = (parent) => Array
        .from(parent.querySelectorAll('text:not(.sequence_diagram_clickable)'))
        // filter out the Test & Setup group boxes if present
        .filter(textNode => {
            let previousElementSibling = textNode.previousElementSibling;

            return previousElementSibling && previousElementSibling.tagName === 'rect' && previousElementSibling.previousElementSibling && previousElementSibling.previousElementSibling.tagName !== 'path';
        })
        .flatMap(textNode => [ textNode, textNode.previousElementSibling ]);

    const nameForActor = (textOrRectNode) => {
        const attributeValue = textOrRectNode.getAttribute('data-actor');
        if (attributeValue) {
            return attributeValue;
        } else {
            const textNode = textOrRectNode.tagName === 'text' ? textOrRectNode : textOrRectNode.nextElementSibling;
            return textNode.textContent;
        }
    }

    const annotateActor = (actorNode) => {
        try {
            const actorName = nameForActor(actorNode);
            actorNode.classList.add('actor', `actor-${actorName}`);
            actorNode.setAttribute('data-actor', actorName);
        } catch (e) {
            console.error('Failed to annotate actor', actorNode, e);
        }
    }

    const annotateMessage = (labelNode) => {
        try {
            const interactionId = labelNode.getAttribute('sequence_diagram_interaction_id');
            const interaction = capturedInteractions.find(i => i.id === interactionId);

            const [, srcActor, destActor] = interaction.name.substring(labelNode.textContent.length).match(/^ from (.+) to (.+)$/);
            const classNames = ['msg', `msg-from-${srcActor}`, `msg-to-${destActor}`];

            const lineNode = labelNode.previousSibling;
            const arrowNode = lineNode.previousSibling;

            [labelNode, lineNode, arrowNode].forEach(elem => {
                elem.classList.add(...classNames);
            });
        } catch (e) {
            console.error('Failed to annotate message', labelNode, e);
        }
    }

    const addFloatingHeaderTo = (sequenceDiagram) => {
        try {
            const originalSvg = sequenceDiagram.querySelector('svg');
            const newSvg = originalSvg.cloneNode(true);
            const hasInteractionTestGroup = newSvg.querySelector('rect:first-child');

            // Remove all SVG elements except the top row of <rects> and <text>s
            newSvg.querySelectorAll('line, .sequence_diagram_clickable, path, polygon').forEach(element => element.remove());

            // Unless disableInteractionTestGroup() has been called, there will be the following additional elements that
            // we don't want to include in the header:
            //  1. A white <rect> behind everything. This will be the first child in the main <g> element.
            //  2. A <rect> with black border, the same size as #1. This will now be the penultimate child in the main <g> element.
            //  3. The <text> "Test" (or equivalent) in the top-left corner. This will now be the final child in the main <g> element.
            if (hasInteractionTestGroup) {
                newSvg.querySelectorAll('rect:first-child, rect:nth-last-child(2), text:last-child').forEach(element => element.remove());
            }

            // Finally, every 2nd <text> and <rect> is a copy of an actor at the top, and thus can be discarded.
            newSvg.querySelectorAll('text, rect').forEach((element, idx) => { if (idx % 4 >= 2) element.remove() } );

            const firstRect = newSvg.querySelector('rect')
            if (firstRect) {
                const firstRectTop = parseInt(firstRect.getAttribute('y'));
                const firstRectHeight = parseInt(firstRect.getAttribute('height'));
                const headerHeight = (firstRectTop * 2) + firstRectHeight;

                newSvg.style.height = `${headerHeight}px`;
                newSvg.setAttribute('viewBox', newSvg.getAttribute('viewBox').split(' ').with(3, `${headerHeight}`).join(' '));
                newSvg.setAttribute('height', `${headerHeight}px`);

                const headerDiv = sequenceDiagram.querySelector('.sequence-diagram-header');
                headerDiv.appendChild(newSvg);
                headerDiv.setAttribute('height', `${headerHeight}`);
            }
        } catch (e) {
            console.error('Failed to add floating header', e);
        }
    }

    /// Using `position: sticky` doesn't work reliably in any browser when any ancestor has `overflow-x: auto` :(
    const updateFloatingHeaderPosition = (sequenceDiagram) => {
        const windowHeight = window.innerHeight;
        const windowScrollTop = window.scrollY;

        const diagramContainerTop = sequenceDiagram.offsetTop
        const diagramContainerHeight = sequenceDiagram.offsetHeight

        const header = sequenceDiagram.querySelector('.sequence-diagram-header');
        const headerHeight = parseInt(header.getAttribute('height'));

        const diagramIsTooBigToFitOnScreen = windowHeight < diagramContainerHeight;
        const userHasReachedDiagram = windowScrollTop >= diagramContainerTop;
        const userHasNotScrolledPastDiagram = windowScrollTop < diagramContainerTop + diagramContainerHeight - (headerHeight * 2);

        if (diagramIsTooBigToFitOnScreen && userHasReachedDiagram && userHasNotScrolledPastDiagram) {
            header.style.top = `${windowScrollTop - diagramContainerTop}px`;
            header.style.display = 'block';
        } else {
            header.style.display = 'none';
        }
    }

    useEffect(() => {
        const sequenceDiagram = sequenceDiagramRef.current;

        addFloatingHeaderTo(sequenceDiagram);

        const updateFloatingHeaderPositionCallback = () => updateFloatingHeaderPosition(sequenceDiagram);
        sequenceDiagram.ownerDocument.addEventListener('scroll', updateFloatingHeaderPositionCallback);

        const actorNodes = actorChildrenOf(sequenceDiagram);
        const clickableMessageNodes = clickableMessageChildrenOf(sequenceDiagram);

        actorNodes.forEach(node => {
            node.addEventListener('click', onActorClick);
            annotateActor(node);
        })

        clickableMessageNodes.forEach(node => {
            node.addEventListener('click', onMessageClick);
            annotateMessage(node);
        })

        return () => {
            actorNodes.forEach(node => {
                node.removeEventListener('click', onActorClick);
            })

            clickableMessageNodes.forEach(node => {
                node.removeEventListener('click', onMessageClick);
            })

            sequenceDiagram.ownerDocument.removeEventListener('scroll', updateFloatingHeaderPositionCallback);
        }
    }, []);

    useEffect(updateHighlightedActorsAndMessages, [selectedActorNames]);

    const classNames = 'sequence-diagram' +
        (selectedActorNames.length > 0 ? ' filtered' : '') +
        (isNoMatchesForFilter ? ' no-matches' : '');

    return <>
        <div ref={sequenceDiagramRef} className={classNames}>
            <div className="sequence-diagram-header"></div>
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