import React, {createRef, memo, useEffect, useMemo, useState} from "react";
import Lowlight from "react-lowlight";
import {highlight} from "../../Highlighting";
import "./RenderedValues.scss";
import {Controls} from "./Controls";

const escapeHtml = (s) => s.replace(/[&<>"']/g, (ch) => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[ch]));
const escapeRegex = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

const buildRawHighlightedHtml = (text, query, matchIndex) => {
    if (!query || matchIndex < 0) return escapeHtml(text);
    const pattern = new RegExp(escapeRegex(query), "gi");
    let count = -1;
    return escapeHtml(text).replace(pattern, (m) => {
        count++;
        if (count === matchIndex) {
            return `<span class="search-highlight">${m}</span>`;
        }
        return m;
    });
};

const useDebounce = (value, delay) => {
    const [debouncedValue, setDebouncedValue] = useState(value);
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);
        return () => clearTimeout(handler);
    }, [value]);
    return debouncedValue;
};

const unhighlight = (root) => {
    const span = root?.querySelector('.search-highlight');
    if (span) {
        const parent = span.parentNode;
        while (span.firstChild) {
            parent.insertBefore(span.firstChild, span);
        }
        parent.removeChild(span);
        parent.normalize();
    }
};

const getAllTextNodes = (root) => {
    const textNodes = [];
    const stack = [root];
    while (stack.length) {
        const node = stack.pop();
        if (node.nodeType === 3) {
            textNodes.push(node);
        } else {
            for (let i = node.childNodes.length - 1; i >= 0; i--) {
                stack.push(node.childNodes[i]);
            }
        }
    }
    return textNodes;
};

const findAllMatches = (root, query, maxMatches = Infinity) => {
    if (!query) return [];
    const matches = [];
    const textNodes = getAllTextNodes(root);
    let count = 0;
    for (const textNode of textNodes) {
        const text = textNode.data;
        if (!text) continue;
        const lowerText = text.toLowerCase();
        const lowerQuery = query.toLowerCase();
        let index = 0;
        while ((index = lowerText.indexOf(lowerQuery, index)) !== -1) {
            matches.push({textNode, start: index, length: query.length});
            index += lowerQuery.length;
            count++;
            if (count >= maxMatches) return matches;
        }
    }
    return matches;
};

const findTextMatches = (text, query, maxMatches = Infinity) => {
    if (!query) return [];
    const matches = [];
    let index = 0;
    const lowerText = text.toLowerCase();
    const lowerQuery = query.toLowerCase();
    let count = 0;
    while ((index = lowerText.indexOf(lowerQuery, index)) !== -1) {
        matches.push({start: index, length: query.length});
        index += lowerQuery.length;
        count++;
        if (count >= maxMatches) break;
    }
    return matches;
};

const highlightMatch = ({textNode, start, length}) => {
    let current = textNode;
    const end = start + length;
    const after = current.splitText(end);
    const match = current.splitText(start);
    const span = document.createElement('span');
    span.className = 'search-highlight';
    span.appendChild(match);
    current.parentNode.insertBefore(span, after);
};

const RenderedValueComponent = ({highlights, value, searchQuery, setSearchQuery}) => {
    const interactionRef = createRef();
    const [isMaximised, setIsMaximised] = useState(false);
    const [isRaw, setIsRaw] = useState(false);
    const [matches, setMatches] = useState([]);
    const [currentMatchIndex, setCurrentMatchIndex] = useState(-1);
    const language = value.language ? value.language : "plainText";

    const debouncedSearchQuery = useDebounce(searchQuery, 300);

    useEffect(() => {
        if (isRaw) return;
        const parent = interactionRef.current?.children[1].firstChild;
        if (parent) highlight(language, parent, highlights);
    }, [highlights, language, isRaw]);

    useEffect(() => {
        const root = interactionRef.current?.querySelector('.scrollable-value');
        if (!root) return;

        unhighlight(root);
        setMatches([]);
        setCurrentMatchIndex(-1);

        if (!debouncedSearchQuery) return;

        const newMatches = isRaw
            ? findTextMatches(value.value, debouncedSearchQuery)
            : findAllMatches(root, debouncedSearchQuery);

        setMatches(newMatches);
        setCurrentMatchIndex(newMatches.length > 0 ? 0 : -1);
    }, [debouncedSearchQuery, isRaw]);

    useEffect(() => {
        if (currentMatchIndex < 0) return;
        const root = interactionRef.current?.querySelector('.scrollable-value');
        if (!root) return;

        if (!isRaw) {
            const match = matches[currentMatchIndex];
            if (match) {
                highlightMatch(match);
            }
        }

        const highlightEl = root.querySelector('.search-highlight');
        if (highlightEl) {
            highlightEl.scrollIntoView({block: 'center', inline: 'nearest', behavior: 'smooth'});
        }
    }, [currentMatchIndex, matches]);

    const rawInnerHtml = useMemo(() => ({
        __html: buildRawHighlightedHtml(value.value, debouncedSearchQuery, currentMatchIndex)
    }), [value.value, debouncedSearchQuery, currentMatchIndex]);

    useEffect(() => {
        if (!isRaw) return;
        const root = interactionRef.current?.querySelector('.scrollable-value');
        const highlightEl = root?.querySelector('.search-highlight');
        if (highlightEl) {
            highlightEl.scrollIntoView({block: 'center', inline: 'nearest', behavior: 'smooth'});
        }
    }, [rawInnerHtml, isRaw]);

    const navigateMatch = (direction) => {
        if (matches.length === 0) return;
        const root = interactionRef.current?.querySelector('.scrollable-value');
        if (!isRaw) {
            unhighlight(root);
        }
        let newIndex = currentMatchIndex + direction;
        if (newIndex < 0) newIndex = matches.length - 1;
        if (newIndex >= matches.length) newIndex = 0;
        setCurrentMatchIndex(newIndex);
    };

    const onCopy = async () => {
        try {
            await navigator.clipboard.writeText(value.value);
        } catch (err) {
            console.error('Failed to copy text: ', err);
        }
    };

    const toggleMaximise = () => {
        setIsMaximised(!isMaximised);
    };

    const onRaw = () => setIsRaw(!isRaw);

    return (
        <div
            className={`rendered-value ${isMaximised ? 'maximized' : ''}`}
            ref={interactionRef}
            tabIndex={isMaximised ? 0 : -1}
            onKeyDown={(e) => {
                if (e.key === "Escape" && isMaximised) {
                    setIsMaximised(false);
                    e.stopPropagation();
                }
            }}
        >
            <Controls
                onRaw={onRaw}
                toggleMaximise={toggleMaximise}
                onCopy={onCopy}
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                isMaximised={isMaximised}
                isRaw={isRaw}
                interactionRef={interactionRef}
                navigateMatch={navigateMatch}
                matchCount={matches.length}
                currentMatchIndex={currentMatchIndex}
            />
            <div className="scrollable-container">
                {isRaw ? (
                    <pre
                        className="scrollable-value"
                        data-raw="true"
                        style={{whiteSpace: 'pre-wrap', margin: 0, padding: '1em', color: 'black'}}
                        dangerouslySetInnerHTML={rawInnerHtml}
                    />
                ) : (
                    <Lowlight className="scrollable-value" language={language} value={value.value}/>
                )}
            </div>
        </div>
    );
};

const RenderedValue = memo(RenderedValueComponent);

const WithTabs = ({values, highlights, searchQuery, setSearchQuery}) => {
    const [selectedTab, setSelectedTab] = useState(values[0].name);

    return (
        <>
            <div className="buttons has-addons are-small">
                {values.map((value, idx) => (
                    <button
                        key={idx}
                        className={`button ${selectedTab === value.name ? 'is-selected' : ''}`}
                        onClick={() => setSelectedTab(value.name)}
                    >
                        {value.name}
                    </button>
                ))}
            </div>
            {values.map((value, idx) => (
                <div key={idx} style={{display: selectedTab === value.name ? 'block' : 'none'}}>
                    <RenderedValue
                        highlights={highlights}
                        value={value}
                        searchQuery={searchQuery}
                        setSearchQuery={setSearchQuery}
                    />
                </div>
            ))}
        </>
    );
};

const NoTabs = ({values, highlights, searchQuery, setSearchQuery}) => (
    <RenderedValue
        highlights={highlights}
        value={values[0]}
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
    />
);

const RenderedValues = ({values, highlights}) => {
    const [searchQuery, setSearchQuery] = useState('');

    return values.length > 1 ? (
        <WithTabs
            values={values}
            highlights={highlights}
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
        />
    ) : (
        <NoTabs
            values={values}
            highlights={highlights}
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
        />
    );
};

export default RenderedValues;