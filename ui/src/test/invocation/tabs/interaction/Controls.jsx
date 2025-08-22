import React, {useEffect, useRef, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faArrowLeft, faArrowRight, faCode, faCopy, faFileLines, faGripVertical, faMaximize, faMinimize, faSearch, faTimes} from "@fortawesome/free-solid-svg-icons";

export const Controls = ({onRaw, toggleMaximise, onCopy, searchQuery, setSearchQuery, isMaximised, isRaw, interactionRef, navigateMatch, matchCount, currentMatchIndex}) => {
    const controlsRef = useRef(null);
    const [position, setPosition] = useState(null);
    const [dragging, setDragging] = useState(false);
    const dragStart = useRef(null);

    const resetPosition = () => setPosition(null);

    const handleToggleMaximise = () => {
        resetPosition();
        toggleMaximise();
    };

    const handleMouseDown = (event) => {
        event.preventDefault();

        const controlsRect = controlsRef.current.getBoundingClientRect();
        let containerRect;

        if (isMaximised) {
            // use viewport coordinates
            containerRect = {
                left: 0,
                top: 0,
                width: window.innerWidth,
                height: window.innerHeight
            };
        } else {
            // use parent container coordinates
            containerRect = interactionRef.current.getBoundingClientRect();
        }

        if (position === null) {
            let initialX = controlsRect.left - containerRect.left;
            let initialY = controlsRect.top - containerRect.top;

            if (isMaximised) {
                const vh5 = window.innerHeight * 0.05;
                const rem075 = parseFloat(getComputedStyle(document.documentElement).fontSize) * 0.75;
                initialY = initialY - vh5 - rem075;
            }

            setPosition({x: initialX, y: initialY});
        }

        // Calculate the mouse position relative to the top-left of the Controls
        dragStart.current = {
            offsetX: event.clientX - controlsRect.left,
            offsetY: event.clientY - controlsRect.top,
        };

        setDragging(true);
    };

    const handleMouseMove = (event) => {
        if (!dragging) return;

        let containerRect, controlsWidth, controlsHeight;

        if (isMaximised) {
            // constrain to viewport
            containerRect = {
                left: 0,
                top: 0,
                width: window.innerWidth,
                height: window.innerHeight
            };
            controlsWidth = controlsRef.current.offsetWidth;
            controlsHeight = controlsRef.current.offsetHeight;
        } else {
            // constrain to parent container
            containerRect = interactionRef.current.getBoundingClientRect();
            controlsWidth = controlsRef.current.offsetWidth;
            controlsHeight = controlsRef.current.offsetHeight;
        }

        let newX = event.clientX - dragStart.current.offsetX - containerRect.left;
        let newY = event.clientY - dragStart.current.offsetY - containerRect.top;

        newX = Math.max(0, Math.min(newX, containerRect.width - controlsWidth));
        newY = Math.max(0, Math.min(newY, containerRect.height - controlsHeight));

        setPosition({x: newX, y: newY});
    };

    const handleMouseUp = () => {
        setDragging(false);
    };

    const handleDoubleClick = () => {
        setPosition(null); // Nullify the state to return to CSS positioning
    };

    useEffect(() => {
        if (dragging) {
            window.addEventListener("mousemove", handleMouseMove);
            window.addEventListener("mouseup", handleMouseUp);
        } else {
            window.removeEventListener("mousemove", handleMouseMove);
            window.removeEventListener("mouseup", handleMouseUp);
        }
        return () => {
            window.removeEventListener("mousemove", handleMouseMove);
            window.removeEventListener("mouseup", handleMouseUp);
        };
    }, [dragging]);

    return (
        <div className="controls"
             ref={controlsRef}
             style={{
                 // When position is set, override CSS positioning completely
                 ...(position !== null ? {
                     transform: `translate(${position.x}px, ${position.y}px)`,
                     top: 0,
                     right: 'auto',
                     left: 0,
                 } : {}),
                 cursor: dragging ? "grabbing" : "grab",
                 opacity: dragging ? 1 : undefined,
             }}
        >
            <div className="field has-addons">
                <div className="control" style={{cursor: 'grab'}}
                     onMouseDown={handleMouseDown}
                     onDoubleClick={handleDoubleClick}>
                    <button className="button is-small is-static">
                       <span className="icon">
                           <FontAwesomeIcon icon={faGripVertical}/>
                       </span>
                    </button>
                </div>
                <div className="control is-expanded has-icons-left has-icons-right">
                    <input
                        className="input is-small"
                        type="text"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === "Escape") {
                                if (searchQuery.trim() === '') {
                                    interactionRef.current.focus();
                                } else {
                                    setSearchQuery('');
                                    e.stopPropagation()
                                }
                            } else if (e.key === 'Enter') {
                                if (e.shiftKey) {
                                    navigateMatch(-1);
                                } else {
                                    navigateMatch(1);
                                }
                                e.preventDefault();
                            }
                        }}
                        placeholder="Search..."
                    />
                    <span className="icon is-small is-left">
                        <FontAwesomeIcon icon={faSearch}/>
                    </span>
                    <span className="icon is-small is-right" onClick={() => setSearchQuery('')} style={{cursor: 'pointer', pointerEvents: 'auto'}}>
                        <FontAwesomeIcon icon={faTimes}/>
                    </span>
                </div>
                <div className="control">
                   <span className="button is-small is-static has-text-centered" style={{width: '80px'}}>
                       {matchCount > 0 ? (matchCount === 1 ? '1 match' : `${currentMatchIndex + 1} of ${matchCount}`) : 'No matches'}
                   </span>
                </div>
                <div className="control">
                    <button
                        className="button is-small"
                        onClick={() => navigateMatch(-1)}
                        disabled={matchCount <= 1}
                        title="Previous match"
                        tabIndex={-1}
                    >
                        <span className="icon">
                            <FontAwesomeIcon icon={faArrowLeft}/>
                        </span>
                    </button>
                </div>
                <div className="control">
                    <button
                        className="button is-small"
                        onClick={() => navigateMatch(1)}
                        disabled={matchCount <= 1}
                        title="Next match"
                        tabIndex={-1}
                    >
                        <span className="icon">
                            <FontAwesomeIcon icon={faArrowRight}/>
                        </span>
                    </button>
                </div>
                <div className="control">
                    <button className="button is-small" title="Copy" onClick={onCopy}>
                        <span className="icon">
                            <FontAwesomeIcon icon={faCopy}/>
                        </span>
                    </button>
                </div>
                <div className="control">
                    <button className="button is-small" onClick={handleToggleMaximise} title={isMaximised ? 'Minimise' : 'Maximise'}>
                        <span className="icon">
                            <FontAwesomeIcon icon={isMaximised ? faMinimize : faMaximize}/>
                        </span>
                    </button>
                </div>
                <div className="control">
                    <button
                        className={`button is-small ${isRaw ? 'has-background-grey-light is-selected' : ''}`}
                        aria-pressed={isRaw}
                        title={isRaw ? 'Formatted' : 'Raw'}
                        onClick={onRaw}
                    >
                        <span className="icon">
                            <FontAwesomeIcon icon={isRaw ? faCode : faFileLines}/>
                        </span>
                    </button>
                </div>
            </div>
        </div>
    );
};