import React, {createContext} from "react";
import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faMinus} from "@fortawesome/free-solid-svg-icons/faMinus";
import {faPlus} from "@fortawesome/free-solid-svg-icons/faPlus";
import {faGithub} from "@fortawesome/free-brands-svg-icons/faGithub";

export const ConfigContext = createContext({})

export const GitHubIcon = () =>
    <FontAwesomeIcon icon={faGithub} size="5x"/>

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

export const Tab = {
    CapturedOutputs: 'capturedOutputs',
    CapturedInteractions: 'capturedInteractions',
    Fixtures: 'fixtures',
    Givens: 'givens',
    Parameters: 'parameters',
    SequenceDiagram: 'sequenceDiagram',
}

export const flash = (elem, numFlashes, flashingClassName = 'flashing', singleFlashDurationMillis = 150) => {
    const restoreAppearanceAndRepeatIfNecessary = () => {
        elem.classList.remove(flashingClassName);

        if (numFlashes > 1) {
            setTimeout(() => flash(elem, numFlashes - 1, flashingClassName, singleFlashDurationMillis), singleFlashDurationMillis);
        }
    }

    elem.classList.add(flashingClassName);
    setTimeout(restoreAppearanceAndRepeatIfNecessary, singleFlashDurationMillis);
}

export const forAllCombinations = (arr, callback) => {
    for (let i = 0; i < arr.length - 1; i++) {
        for (let j = i + 1; j < arr.length; j++) {
            callback(arr[i], arr[j])
        }
    }
}
