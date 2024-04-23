import {faAngleDown} from "@fortawesome/free-solid-svg-icons/faAngleDown";
import {faAngleUp} from "@fortawesome/free-solid-svg-icons/faAngleUp";

const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    return items.map( it => '\\b' + it.replace(specials, '\\$&') + '\\b').join('|')
}


export const hasElements = (invocation, name) => {
    let invocationElement = invocation[name];
    return invocationElement && invocationElement.length > 0;
}

export const collapseIcon = (isCollapsed) => (isCollapsed) ? faAngleDown : faAngleUp

export const stateClassFor = state => "test-" + state.toLowerCase();