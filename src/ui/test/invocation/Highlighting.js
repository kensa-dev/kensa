const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    return items.map(it => '\\b' + it.replace(specials, '\\$&') + '\\b').join('|')
}

const createSpan = (value) => {
    let span = document.createElement('span');
    span.setAttribute('class', 'kensa-highlight');
    span.appendChild(textNodeOf(value));
    return span;
};

const createWrappingSpan = (contentNode) => {
    let span = document.createElement('span');
    span.appendChild(contentNode);
    return span;
};

const textNodeOf = (value) => {
    return document.createTextNode(value);
};

const searchElementContent = (parent, child, regExp) => {
    let lastChild = child;
    let restOfLine = child.textContent;
    let result;
    let textNode;
    // noinspection JSAssignmentUsedAsCondition
    while (result = regExp.exec(restOfLine)) {
        let token = result[0];
        let index = result.index;

        if (index === 0) {
            // debugger;
            restOfLine = restOfLine.substring(index + token.length);
            textNode = textNodeOf(restOfLine);
            parent.replaceChild(textNode, lastChild);
            parent.insertBefore(createSpan(token), textNode);
        } else {
            lastChild.textContent = restOfLine.substring(0, result.index);
            let span = createSpan(token);
            parent.insertBefore(span, lastChild.nextSibling);
            restOfLine = restOfLine.substring(index + token.length);
            textNode = textNodeOf(restOfLine);
            parent.insertBefore(textNode, span.nextSibling);
        }

        lastChild = textNode;
    }
};

const searchAttributeContent = (parent, regExp) => {
    let lastChild = parent.firstChild;
    let restOfLine = parent.textContent;
    let result;
    // noinspection JSAssignmentUsedAsCondition
    while (result = regExp.exec(restOfLine)) {
        let token = result[0];
        let index = result.index;
        let textNode = textNodeOf(restOfLine.substring(0, result.index));
        parent.replaceChild(textNode, lastChild);
        parent.appendChild(createSpan(token));
        restOfLine = restOfLine.substring(index + token.length);
        textNode = textNodeOf(restOfLine);
        parent.appendChild(textNode);
        lastChild = textNode;
    }
};

export const highlight = (language, parent, highlights) => {
    const highlightRegexp = highlights.length > 0 ? new RegExp(`(${joinForRegex(highlights)})`) : null;

    if (highlightRegexp) {
        if (language === 'xml') {
            highlightXml(parent, highlightRegexp);
        } else if (language === 'json') {
            highlightJson(parent, highlightRegexp);
        } else {
            highlightPlainText(parent, highlightRegexp);
        }
    }
}

const highlightXml = (parent, regExp) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child.classList) {
            if (child.classList.contains('hljs-tag')) {
                highlightXml(child, regExp);
            } else if (child.classList.contains('hljs-attr')) {
                searchAttributeContent(child.nextSibling.nextSibling, regExp);
            }
        } else {
            searchElementContent(parent, child, regExp);
        }
    });
};

const highlightJson = (parent, regExp) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child.classList) {
            if (child.classList.contains('hljs-string') || child.classList.contains('hljs-number')) {
                searchAttributeContent(child, regExp);
            }
        }
    });
};

const highlightPlainText = (parent, regExp) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child) {
            let parent = child.parentNode;
            child.remove();
            let outerSpan = createWrappingSpan(child);
            parent.appendChild(outerSpan)
            let restOfLine = child.textContent;
            let result;
            let lastChild = outerSpan;
            // noinspection JSAssignmentUsedAsCondition
            while (result = regExp.exec(restOfLine)) {
                let token = result[0];
                let index = result.index;
                let textNode = textNodeOf(restOfLine.substring(0, result.index));
                lastChild.replaceChild(textNode, child);
                lastChild.appendChild(createSpan(token));

                restOfLine = restOfLine.substring(index + token.length);
                textNode = textNodeOf(restOfLine);
                lastChild.appendChild(textNode);
                child = textNode;
            }
        }
    });
};
