const createSpan = (value) => {
    let span = document.createElement('span');
    span.setAttribute('class', 'kensa-highlight');
    span.appendChild(textNodeOf(value));
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
            restOfLine = restOfLine.substr(index + token.length);
            textNode = textNodeOf(restOfLine);
            parent.replaceChild(textNode, lastChild);
            parent.insertBefore(createSpan(token), textNode);
        } else {
            lastChild.textContent = restOfLine.substr(0, result.index);
            let span = createSpan(token);
            parent.insertBefore(span, lastChild.nextSibling);
            restOfLine = restOfLine.substr(index + token.length);
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
        let textNode = textNodeOf(restOfLine.substr(0, result.index));
        parent.replaceChild(textNode, lastChild);
        parent.appendChild(createSpan(token));
        restOfLine = restOfLine.substr(index + token.length);
        textNode = textNodeOf(restOfLine);
        parent.appendChild(textNode);
        lastChild = textNode;
    }
};

export const highlightXml = (parent, regExp) => {
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

export const highlightJson = (parent, regExp) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child.classList) {
            if (child.classList.contains('hljs-string')) {
                searchAttributeContent(child, regExp);
            }
        }
    });
};
