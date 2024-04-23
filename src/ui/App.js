import React, {Fragment, useEffect, useState} from 'react';
import './App.scss';
import TestWrapper from "./Test";
import Indices from "./IndexPage";

const Mode = {
    IndexFile: 'IndexFile',
    TestFile: 'TestFile'
};

export const Section = {
    Buttons: 'Buttons',
    Exception: 'Exception',
    Sentences: 'Sentences'
}

function linkFor(match) {
    let methodLink = ""
    if (match.length > 2) {
        methodLink = "#" + match[3]
    }
    return (<a href={"./" + match[1] + ".html" + methodLink}>{match[1] + methodLink}</a>)
}

function parseNotes(matches, notes) {
    let lastIndex = 0
    let fragments = matches.map((match) => {
        let prefix = null
        if (lastIndex < match.index) {
            prefix = <span>{notes.substr(lastIndex, match.index - lastIndex)}</span>
        }
        lastIndex = match.index + match[0].length
        return (
                <Fragment>
                    {prefix}
                    {linkFor(match)}
                </Fragment>
        )
    });

    return {fragments: fragments, lastIndex: lastIndex}
}

function suffix(lastIndex, notes) {
    if (lastIndex < notes.length) {
        return <span>{notes.substr(lastIndex, notes.length)}</span>
    }
}

export function makeNotes(notes) {
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

export function stateClassFor(state) {
    return "test-" + state.toLowerCase();
}

function sectionOrderFrom(json) {
    return json.sectionOrder.map((value) => Section[value])
}

function selectModeFrom(json) {
    return Mode[json.mode];
}

export default function App() {
    const [isLoaded, setIsLoaded] = useState(false);
    const [mode, setMode] = useState(null);
    const [issueTrackerUrl, setIssueTrackerUrl] = useState("#");
    const [sectionOrder, setSectionOrder] = useState([]);
    const [data, setData] = useState(null);
    const [indices, setIndices] = useState(null);

    useEffect(() => {
        let configNode = document.querySelector("script[id='config']");
        let configJson = JSON.parse(configNode.textContent);
        let mode = selectModeFrom(configJson);
        let sectionOrder = sectionOrderFrom(configJson)

        let indices = null;
        let data = null;

        switch (mode) {
            case Mode.TestFile:
                data = JSON.parse(document.querySelector("script[id^='test-result']").textContent);
                break;
            case Mode.IndexFile:
                let indexNode = document.querySelector("script[id='indices']");
                let indexJson = JSON.parse(indexNode.textContent);
                indices = indexJson.indices;
        }

        let issueTrackerUrl = configJson["issueTrackerUrl"];
        setIssueTrackerUrl(issueTrackerUrl == null ? "#" : issueTrackerUrl);
        setMode(mode);
        setSectionOrder(sectionOrder);
        setData(data);
        setIndices(indices);
        setIsLoaded(true);
    }, []);

    const issueTrackerUrlFor = (issue) => {
        if (issueTrackerUrl.endsWith("/")) {
            return issueTrackerUrl + issue;
        }

        return issueTrackerUrl + "/" + issue;
    }

    const renderIndexFile = () => {
        return (
            <div>
                <section className="hero test-passed">
                    <div className="hero-body">
                        <h1 className="title">Index</h1>
                    </div>
                </section>
                <section className="section">
                    <Indices issueTrackerUrl={issueTrackerUrl} indices={indices}/>
                </section>
            </div>
        )
    }

    const renderIssues = (issues) => {
        return <div className="tags">
            {
                issues.map(issue => {
                    return <a href={issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                })
            }
        </div>;
    }

    const renderInformation = (issues, notes) => {
        let issueContent = issues.length > 0 ? renderIssues(issues) : null;
        let notesContent = notes ? makeNotes(notes) : null;

        if (issues.length > 0 || notes) {
            return (
                <div className="message is-info">
                    <div className="message-body">
                        {issueContent}
                        {notesContent}
                    </div>
                </div>
            )
        }

        return null;
    }

    const renderTestFile = () => {
        return (
            <div>
                <section className={"hero " + stateClassFor(data.state)}>
                    <div className="hero-body">
                        <h1 className="title">{data.displayName}</h1>
                    </div>
                </section>
                <section className="section">
                    {renderInformation(data.issues, data.notes, data.state)}
                    <TestWrapper issueTrackerUrlFor={issueTrackerUrlFor} sectionOrder={sectionOrder} tests={data.tests}/>
                </section>
            </div>
        )
    }

    if (isLoaded) {
        switch (mode) {
            case Mode.IndexFile:
                return renderIndexFile();
            case Mode.TestFile:
                return renderTestFile();
        }
    } else {
        return (<div>Loading...</div>)
    }
}
