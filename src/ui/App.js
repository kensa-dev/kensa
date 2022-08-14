import React, {Component, Fragment} from 'react';
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

export default class App extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isLoaded: false
        };
    }

    static stateClassFor(state) {
        return "test-" + state.toLowerCase();
    }

    static sectionOrderFrom(json) {
        return json.sectionOrder.map((value) => Section[value])
    }

    static selectModeFrom(json) {
        return Mode[json.mode];
    }

    componentDidMount() {
        let configNode = document.querySelector("script[id='config']");
        let configJson = JSON.parse(configNode.textContent);
        let mode = App.selectModeFrom(configJson);
        let sectionOrder = App.sectionOrderFrom(configJson)

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
        this.setState({
            issueTrackerUrl: issueTrackerUrl == null ? "#" : issueTrackerUrl,
            mode: mode,
            sectionOrder: sectionOrder,
            data: data,
            indices: indices,
            isLoaded: true
        });
    }

    renderIndexFile() {
        return (
                <div>
                    <section className="hero test-passed">
                        <div className="hero-body">
                            <h1 className="title">Index</h1>
                        </div>
                    </section>
                    <section className="section">
                        <Indices issueTrackerUrl={this.state.issueTrackerUrl} indices={this.state.indices}/>
                    </section>
                </div>
        )
    }

    issueTrackerUrlFor(issue) {
        let issueTrackerUrl = this.state.issueTrackerUrl;

        if (issueTrackerUrl.endsWith("/")) {
            return issueTrackerUrl + issue;
        }

        return issueTrackerUrl + "/" + issue;
    }

    renderIssues(issues) {
        return <div className="tags">
            {
                issues.map(issue => {
                    return <a href={this.issueTrackerUrlFor(issue)} className={"tag is-small has-background-grey has-text-white"}>{issue}</a>
                })
            }
        </div>;
    }

    renderInformation(issue, notes, state) {
        let issueContent = issue.length > 0 ? this.renderIssues(issue) : null;
        let notesContent = notes ? makeNotes(notes) : null;

        if (issue.length > 0 || notes) {
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

    renderTestFile() {
        const data = this.state.data;

        let info = this.renderInformation(data.issue, data.notes, data.state)
        return (
                <div>
                    <section className={"hero " + App.stateClassFor(data.state)}>
                        <div className="hero-body">
                            <h1 className="title">{data.displayName}</h1>
                        </div>
                    </section>
                    <section className="section">
                        {info}
                        <TestWrapper issueTrackerUrl={this.state.issueTrackerUrl} sectionOrder={this.state.sectionOrder} tests={data.tests}/>
                    </section>
                </div>
        );
    }

    render() {
        if (this.state.isLoaded) {
            switch (this.state.mode) {
                case Mode.IndexFile:
                    return this.renderIndexFile();
                case Mode.TestFile:
                    return this.renderTestFile();
            }
        } else {
            return (<div>Loading...</div>)
        }
    }
}