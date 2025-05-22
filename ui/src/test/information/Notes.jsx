import React, {Fragment} from "react";

const parseNotes = (matches, notes) => {
    let lastIndex = 0
    let fragments = matches.map((match, idx) => {
        let prefix = null
        if (lastIndex <= match.index) {
            prefix = <span>{notes.substring(lastIndex, match.index - lastIndex)}</span>
        }
        lastIndex = match.index + match[0].length
        return (
            <Fragment key={idx}>
                {prefix}
                {linkFor(match)}
            </Fragment>
        )
    });

    return {fragments: fragments, lastIndex: lastIndex}
};

const suffix = (lastIndex, notes) => {
    if (lastIndex < notes.length) {
        return <span>{notes.substring(lastIndex, notes.length)}</span>
    }
};

const linkFor = match => {
    let methodLink = ""
    if (match.length > 2) {
        methodLink = "#" + match[3]
    }
    return (<a href={"./" + match[1] + ".html" + methodLink}>{match[1] + methodLink}</a>)
};

const Notes = ({notes}) => {
    if (notes) {
        const regex = /{\s*@link\s+([a-zA-Z0-9._$]+)(#([a-zA-Z0-9._$]+))?\s*}/g
        const matches = [...notes.matchAll(regex)]
        let parsedNotes = parseNotes(matches, notes)

        return (
            <Fragment>
                {parsedNotes.fragments}
                {suffix(parsedNotes.lastIndex, notes)}
            </Fragment>
        )
    }
}

export default Notes