import React, {createContext} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";

const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export const TrackingUrlContext = createContext()
export const SectionOrderContext = createContext()

export function joinForRegex(items) {
    return items.map(it => '\\b' + it.replace(specials, '\\$&') + '\\b').join('|')
}

export const CollapseIcon = ({isCollapsed}) =>
    <FontAwesomeIcon icon={isCollapsed ? faAngleDown : faAngleUp}/>

export const ExpandIcon = ({isExpanded, onClick}) =>
    <FontAwesomeIcon onClick={onClick} className={"idx-icon"} icon={isExpanded ? faMinus : faPlus}/>

export const stateClassFor = state => "test-" + state.toLowerCase();

export const Section = {
    Buttons: 'Buttons',
    Exception: 'Exception',
    Sentences: 'Sentences'
}