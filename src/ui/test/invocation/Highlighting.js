const specials = /[-\/\\^$*+?.()|[\]{}]/g;

export function joinForRegex(items) {
    return items.map(it => '\\b' + it.replace(specials, '\\$&') + '\\b').join('|')
}

const createSpan = (value, highlightDescriptorsByValue) => {
    const highlightDescriptor = highlightDescriptorsByValue[value];
    let span = document.createElement('span');
    span.setAttribute('class', `kensa-highlight kensa-highlight-${highlightDescriptor.colourIndex}`);
    span.setAttribute('title', highlightDescriptor.name);
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

const searchElementContent = (parent, child, regExp, highlightDescriptorsByValue) => {
    let lastChild = child;
    let restOfLine = child.textContent;
    let result;
    let textNode;
    // noinspection JSAssignmentUsedAsCondition
    while (result = regExp.exec(restOfLine)) {
        let token = result[0];
        let index = result.index;

        if (index === 0) {
            restOfLine = restOfLine.substring(index + token.length);
            textNode = textNodeOf(restOfLine);
            parent.replaceChild(textNode, lastChild);
            parent.insertBefore(createSpan(token, highlightDescriptorsByValue), textNode);
        } else {
            lastChild.textContent = restOfLine.substring(0, result.index);
            let span = createSpan(token, highlightDescriptorsByValue);
            parent.insertBefore(span, lastChild.nextSibling);
            restOfLine = restOfLine.substring(index + token.length);
            textNode = textNodeOf(restOfLine);
            parent.insertBefore(textNode, span.nextSibling);
        }

        lastChild = textNode;
    }
};

const searchAttributeContent = (parent, regExp, highlightDescriptorsByValue) => {
    let lastChild = parent.firstChild;
    let restOfLine = parent.textContent;
    let result;
    // noinspection JSAssignmentUsedAsCondition
    while (result = regExp.exec(restOfLine)) {
        let token = result[0];
        let index = result.index;
        let textNode = textNodeOf(restOfLine.substring(0, result.index));
        parent.replaceChild(textNode, lastChild);
        parent.appendChild(createSpan(token, highlightDescriptorsByValue));
        restOfLine = restOfLine.substring(index + token.length);
        textNode = textNodeOf(restOfLine);
        parent.appendChild(textNode);
        lastChild = textNode;
    }
};

export const highlight = (language, parent, highlights) => {
    const highlightRegexp = highlights.length > 0 ? new RegExp(`(${joinForRegex(highlights.map(descriptor => descriptor.value))})`) : null;
    
    const highlightDescriptorsByValue = keyedByValue(mergeAmbiguousHighlightValues(highlights)); 
    
    if (highlightRegexp) {
        if (language === 'xml') {
            highlightXml(parent, highlightRegexp, highlightDescriptorsByValue);
        } else if (language === 'json') {
            highlightJson(parent, highlightRegexp, highlightDescriptorsByValue);
        } else {
            highlightPlainText(parent, highlightRegexp, highlightDescriptorsByValue);
        }
    }
}

const highlightXml = (parent, regExp, highlightDescriptorsByValue) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child.classList) {
            if (child.classList.contains('hljs-tag')) {
                highlightXml(child, regExp, highlightDescriptorsByValue);
            } else if (child.classList.contains('hljs-attr')) {
                searchAttributeContent(child.nextSibling.nextSibling, regExp, highlightDescriptorsByValue);
            }
        } else {
            searchElementContent(parent, child, regExp, highlightDescriptorsByValue);
        }
    });
};

const highlightJson = (parent, regExp, highlightDescriptorsByValue) => {
    Array.from(parent.childNodes).forEach(child => {
        if (child.classList) {
            if (child.classList.contains('hljs-string') || child.classList.contains('hljs-number')) {
                searchAttributeContent(child, regExp, highlightDescriptorsByValue);
            }
        }
    });
};

const highlightPlainText = (parent, regExp, highlightDescriptorsByValue) => {
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
                lastChild.appendChild(createSpan(token, highlightDescriptorsByValue));

                restOfLine = restOfLine.substring(index + token.length);
                textNode = textNodeOf(restOfLine);
                lastChild.appendChild(textNode);
                child = textNode;
            }
        }
    });
};

const groupedByHighlightValue = (highlightDescriptors) => highlightDescriptors
    .reduce((result, descriptor) => { 
            if (!result.has(descriptor.value)) { 
                result.set(descriptor.value, []) 
            } 
            result.get(descriptor.value).push(descriptor); 
            return result; 
        }, new Map());

const mergeAmbiguousHighlightValues = (highlightDescriptors) => groupedByHighlightValue(highlightDescriptors)
        .values()
        .map( descriptorsWithSameValue => {
            if (descriptorsWithSameValue.length === 1) {
                return descriptorsWithSameValue[0];
            } else {
                const description = descriptorsWithSameValue.map(descriptor => `'${descriptor.name}'`).join(" or ") + " (ambiguous)";
                return {
                    name: description,
                    value: descriptorsWithSameValue[0].value,
                    colourIndex: "ambiguous"
                };  
            }
        });

const keyedByValue = (highlightDescriptors) => highlightDescriptors
    .reduce((result, descriptor) => { result[descriptor.value] = descriptor; return result; }, {});
